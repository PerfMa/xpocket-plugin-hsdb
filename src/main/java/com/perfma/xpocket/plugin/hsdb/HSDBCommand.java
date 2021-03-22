package com.perfma.xpocket.plugin.hsdb;

import com.perfma.xlab.xpocket.spi.XPocketPlugin;
import com.perfma.xlab.xpocket.spi.command.AbstractXPocketCommand;
import com.perfma.xlab.xpocket.spi.command.CommandList;
import com.perfma.xlab.xpocket.spi.process.XPocketProcess;

/**
 *
 * @author gongyu <yin.tong@perfma.com>
 */
@CommandList(names = {"clhsdb",
    "assert",
    "attach",
    "buildreplayjars",
    "detach",
    "dis",
    "disassemble",
    "dumpcfg",
    "dumpcodecache",
    "dumpideal",
    "dumpilt",
    "dumpreplaydata",
    "echo",
    "examine",
    "field",
    "findpc",
    "flags",
    "help",
    "history",
    "inspect",
    "int",
    "jdis",
    "jhisto",
    "jstack",
    "livenmethods",
    "long",
    "pmap",
    "print",
    "printall",
    "printas",
    "printmdo",
    "printstatics",
    "pstack",
    "quit",
    "reattach",
    "revptrs",
    "scanoops",
    "search",
    "source",
    "symbol",
    "symboldump",
    "symboltable",
    "thread",
    "threads",
    "tokenize",
    "type",
    "universe",
    "verbose",
    "versioncheck",
    "vmstructsdump",
    "where",
    "class",
    "classes",
    "jseval",
    "jsload",
    "mem",
    "sysprops",
    "universe",
    "whatis", "classdetail", "instance", "method", "objectvisit", "stack", "typedetail"},
        usage = {"clhsdb [path of sa-jdi.jar] start hsdb command line",
            "assert true | false",
            "attach pid | exec core",
            "buildreplayjars [ all | app | boot ] | [ prefix ]",
            "detach",
            "dis address [length]",
            "disassemble address",
            "dumpcfg { -a | id }",
            "dumpcodecache",
            "dumpideal { -a | id }",
            "dumpilt { -a | id }",
            "dumpreplaydata { <address > | -a | <thread_id> }",
            "echo [ true | false ]",
            "examine [ address/count ] | [ address,address]",
            "field [ type [ name fieldtype isStatic offset address ] ]",
            "findpc address",
            "flags [ flag | -nd ]",
            "help [ command ]",
            "history",
            "inspect expression",
            "intConstant [ name [ value ] ]",
            "jdis address",
            "jhisto",
            "jstack [-v]",
            "livenmethods",
            "longConstant [ name [ value ] ]",
            "pmap",
            "print expression",
            "printall",
            "printas type expression",
            "printmdo [ -a | expression ]",
            "printstatics [ type ]",
            "pstack [-v]",
            "quit",
            "reattach",
            "revptrs address",
            "scanoops start end [ type ]",
            "search [ heap | perm | rawheap | codecache | threads ] value",
            "source filename",
            "symbol address",
            "symboldump",
            "symboltable name",
            "thread { -a | id }",
            "threads",
            "tokenize ...",
            "type [ type [ name super isOop isInteger isUnsigned size ] ]",
            "universe",
            "verbose true | false",
            "versioncheck [ true | false ]",
            "vmstructsdump",
            "where { -a | id }",
            "class name",
            "classes",
            "jseval script",
            "jsload file",
            "mem address [ length ]",
            "sysprops",
            "universe",
            "whatis address",
            "classdetail  [address | fullClassName]",
            "instance address",
            "method address",
            "objectvisit [-d] fullClassName[.class]",
            "stack threadName",
            "typedetail typeName"})
public class HSDBCommand extends AbstractXPocketCommand {

    private HSDBPlugin plugin;
    
    @Override
    public void invoke(XPocketProcess process) throws Throwable {
        plugin.invoke(process);
    }

    @Override
    public boolean isAvailableNow(String cmd) {
        return plugin.isAvaibleNow(cmd);
    }

    @Override
    public void init(XPocketPlugin plugin) {
        this.plugin = (HSDBPlugin)plugin;
    }
 
}
