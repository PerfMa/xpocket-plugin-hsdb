package com.perfma.xpocket.plugin.hsdb;

import com.perfma.xlab.xpocket.spi.AbstractXPocketPlugin;
import com.perfma.xlab.xpocket.spi.context.SessionContext;
import com.perfma.xlab.xpocket.spi.process.XPocketProcess;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 *
 * @author gongyu <yin.tong@perfma.com>
 */
public class HSDBPlugin extends AbstractXPocketPlugin implements Runnable {

    private static final String LOGO
            = "  _   _   ____    ____    ____  \n"
            + " | | | | / ___|  |  _ \\  | __ ) \n"
            + " | |_| | \\___ \\  | | | | |  _ \\ \n"
            + " |  _  |  ___) | | |_| | | |_) |\n"
            + " |_| |_| |____/  |____/  |____/ \n";

    private static String JAVA_HOME;

    public static final String lineSeparator = System.getProperty("line.separator");

    public static final String javaVersion = System.getProperty("java.version");

    public static final String osName = System.getProperty("os.name");

    private static final String START_CMD_TEMP;

    private static boolean moduleMode = false;

    private static final Set<String> notNeedAttach = new TreeSet<>();

    private Process clhsdbProc;

    private LinkedBlockingQueue<XPocketProcess> processes = new LinkedBlockingQueue<>();

    private SessionContext context;

    private boolean attachStatus = false;

    private int pid = -1;

    static {

        int version = 8;

        try {
            if (javaVersion.startsWith("1.")) {
                version = Integer.parseInt(javaVersion.substring(javaVersion.indexOf('.') + 1, javaVersion.indexOf('.', 2)));
            } else {
                if(javaVersion.indexOf('.') > 0) {
                    version = Integer.parseInt(javaVersion.substring(0, javaVersion.indexOf('.')));
                } else if (javaVersion.indexOf('-') > 0) {
                    version = Integer.parseInt(javaVersion.substring(0, javaVersion.indexOf('-')));
                } else {
                    version = Integer.parseInt(javaVersion);
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        if (version <= 8) {
            if (osName.toUpperCase().startsWith("WIN")) {
                START_CMD_TEMP = "java -cp \"." + File.pathSeparator + "%s\" sun.jvm.hotspot.CLHSDB %s";
            } else {
                START_CMD_TEMP = "java -cp ." + File.pathSeparator + "%s sun.jvm.hotspot.CLHSDB %s";
            }
        } else {
            moduleMode = true;
            START_CMD_TEMP = "jhsdb clhsdb %s";
        }

        String javaHome = System.getProperty("java.home");
        if (javaHome.endsWith(File.separator + "jre")) {
            JAVA_HOME = javaHome.substring(0, javaHome.lastIndexOf(File.separator));
        }
    }

    @Override
    public void init(XPocketProcess process) {
        String[] notNeedAttachCommand = {"assert", "attach", "echo", "help",
            "history", "quit", "versioncheck", "verbose"};
        notNeedAttach.addAll(Arrays.asList(notNeedAttachCommand));
    }

    @Override
    public void switchOn(SessionContext context) {
        this.context = context;
        context.setPid(pid);
    }

    @Override
    public void destory() throws Throwable {
        if (clhsdbProc != null && clhsdbProc.isAlive()) {
            clhsdbProc.destroyForcibly();
        }
    }

    @Override
    public void run() {
        try {
            InputStream instr = clhsdbProc.getInputStream();
            XPocketProcess process = processes.take();
            process.output("Now you can use \"help\" to show commands of hsdb.");
            try {
                int ret_read = 0, index = 0;
                byte[] line = new byte[1024];

                LOOP:
                for (;;) {
                    ret_read = instr.read();
                    if (ret_read == -1) {
                        break;
                    }

                    if (process == null) {
                        process = processes.take();
                    }

                    switch (ret_read) {
                        case '\r':
                        case '\n':
                            String lineStr = new String(line, 0, index);
                            if (!lineStr.trim().equalsIgnoreCase(process.getCmd())) {
                                process.output(lineStr + lineSeparator);
                            }
                            index = 0;
                            break;
                        case '>':
                            line[index++] = (byte) ret_read;
                            LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(100, TimeUnit.MILLISECONDS));
                            lineStr = new String(line,0, index);
                            if (lineStr.endsWith("hsdb>")) {
                                if(index > 5) {
                                    lineStr = new String(line,0, index - 5);
                                    process.output(lineStr + lineSeparator);
                                }
                                process.end();
                                process = null;
                                index = 0;
                            }
                            break;
                        default:
                            line[index++] = (byte) ret_read;
                    }

                }
                if (process != null) {
                    process.end();
                }
            } catch (IOException e) {
                process.output("Exception while reading socket:" + e.getMessage());
                process.end();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public boolean isAvaibleNow(String cmd) {
        if (clhsdbProc == null || !clhsdbProc.isAlive()) {
            return "clhsdb".equals(cmd);
        } else if (!attachStatus) {
            return !"clhsdb".equals(cmd) && notNeedAttach.contains(cmd);
        } else {
            return !"clhsdb".equals(cmd) && !"attach".equals(cmd);
        }
    }

    public void invoke(XPocketProcess process) {
        try {
            String command = process.getCmd();
            processes.add(process);

            switch (command) {
                case "clhsdb":
                    String[] args = process.getArgs();
                    String hsdbjarPath = null;

                    String[] input = new String[args.length];
                    int pos = 0;
                    for (int i = 0; i < args.length; i++) {
                        String arg = args[i];
                        switch (arg) {
                            case "-d":
                                i++;
                                hsdbjarPath = args[i];
                                break;
                            default:
                                if (pos < input.length) {
                                    input[pos++] = arg;
                                }
                        }
                    }

                    String params = "";

                    for (String arg : input) {
                        if (arg != null && !arg.trim().isEmpty()) {
                            params += arg + " ";
                        }
                    }

                    if (moduleMode) {
                        clhsdbProc = Runtime.getRuntime().exec(
                                    String.format(START_CMD_TEMP, params));
                        Thread t = new Thread(this);
                        t.start();
                    } else {
                        if (hsdbjarPath == null) {
                            hsdbjarPath = JAVA_HOME + "/lib/sa-jdi.jar";
                        }
                        File file = new File(hsdbjarPath);
                        if (file.exists()) {
                            clhsdbProc = Runtime.getRuntime().exec(
                                    String.format(START_CMD_TEMP, hsdbjarPath, params));
                            Thread t = new Thread(this);
                            t.start();
                        } else {
                            process.output("ERROR : sa-jdi.jar is not exist : " + hsdbjarPath);
                            process.end();
                        }
                    }

                    break;
                case "attach":
                    clhsdbProc.getOutputStream().write(handleCmd(process.getCmd(),
                            process.getArgs()));
                    clhsdbProc.getOutputStream().flush();
                    this.attachStatus = true;
                    pid = Integer.parseInt(process.getArgs()[0]);
                    context.setPid(pid);
                    break;
                case "quit":
                case "detach":
                    clhsdbProc.getOutputStream().write(handleCmd(process.getCmd(),
                            process.getArgs()));
                    clhsdbProc.getOutputStream().flush();
                    this.attachStatus = false;
                    pid = -1;
                    context.setPid(pid);
                    break;
                default:
                    clhsdbProc.getOutputStream().write(handleCmd(process.getCmd(),
                            process.getArgs()));
                    clhsdbProc.getOutputStream().flush();
            }

        } catch (Throwable ex) {
            processes.remove(process);
            process.output(ex.getMessage());
            process.end();
        }
    }

    private VirtualMachine findTarget(String pid) throws Throwable {

        VirtualMachineDescriptor virtualMachineDescriptor = null;
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            if (pid.equals(descriptor.id())) {
                virtualMachineDescriptor = descriptor;
                break;
            }
        }
        VirtualMachine virtualMachine;

        if (null == virtualMachineDescriptor) { // 使用 attach(String pid) 这种方式
            virtualMachine = VirtualMachine.attach(pid);
        } else {
            virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
        }

        return virtualMachine;

    }

    private byte[] handleCmd(String cmd, String[] args) {
        return handleCmdStr(cmd, args).getBytes();
    }

    private String handleCmdStr(String cmd, String[] args) {
        StringBuilder cmdStr = new StringBuilder(cmd);

        if (args != null) {
            for (String arg : args) {
                cmdStr.append(' ').append(arg);
            }
        }

        cmdStr.append(lineSeparator);

        return cmdStr.toString();
    }

    @Override
    public void printLogo(XPocketProcess process) {
        process.output(LOGO);
    }

}
