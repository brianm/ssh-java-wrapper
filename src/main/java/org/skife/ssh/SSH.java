package org.skife.ssh;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class SSH
{
    private final boolean inheritOut;
    private final boolean inheritErr;

    private final String       ssh;
    private final List<String> args;
    private final String       host;
    private final String       user;

    private SSH(String user, String ssh, Iterable<String> args, String host, boolean out, boolean err)
    {
        this.user = user.length() != 0 ? (user.contains("@") ? user : user + "@") : "";
        this.ssh = ssh;
        this.host = host;
        this.inheritOut = out;
        this.inheritErr = err;
        this.args = ImmutableList.copyOf(args);
    }

    public ProcessResult exec(String... cmd) throws IOException, InterruptedException
    {
        List<String> argv = Lists.newArrayList("ssh");
        argv.addAll(args);
        argv.add(user + host);
        argv.addAll(Arrays.asList(cmd));
        return ProcessResult.run(inheritOut, inheritErr, argv.toArray(new String[argv.size()]));
    }

    public ProcessResult scp(File from, File to) throws IOException, InterruptedException
    {
        List<String> argv = Lists.newArrayList("scp");
        argv.addAll(args);
        argv.add(from.getAbsolutePath());
        argv.add(user + host + ":" + to.getAbsolutePath());
        return ProcessResult.run(inheritOut, inheritErr, argv.toArray(new String[argv.size()]));
    }

    public SSH withHost(String host)
    {
        return new SSH(user, ssh, args, host, inheritOut, inheritErr);
    }

    public static SSH toHost(String host)
    {
        return new SSH("", "ssh", Collections.<String>emptyList(), host, false, false);
    }

    public SSH withArgs(String... args)
    {
        return new SSH(user, ssh, Iterables.concat(this.args, asList(args)), host, inheritOut, inheritErr);
    }

    public SSH withSshCommand(String ssh)
    {
        return new SSH(user, ssh, args, host, inheritOut, inheritErr);
    }

    public SSH withUser(String user)
    {
        List<String> nargs = Lists.newArrayList(args);
        return new SSH(user, ssh, nargs, host, inheritOut, inheritErr);
    }

    public SSH withUserKnownHostsFile(File file)
    {
        return new SSH(user, ssh, Iterables.concat(args, asList("-o", "UserKnownHostsFile=" + file.getAbsolutePath())), host, inheritOut, inheritErr);
    }

    public SSH withStrictHostKeyChecking(boolean yesno)
    {
        return new SSH(user,
                       ssh,
                       Iterables.concat(args, asList("-o", "StrictHostKeyChecking=" + (yesno ? "yes" : "no"))),
                       host, inheritOut, inheritErr);
    }

    Process dashN()
    {
        List<String> argv = Lists.newArrayList("ssh");
        argv.addAll(args);
        argv.add("-N");
        argv.add(host);
        try {
            return new ProcessBuilder(argv).start();
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public SSH inheritStandardErr()
    {
        return new SSH(user, ssh, args, host, inheritOut, true);
    }

    public SSH withConfigFile(File file)
    {
        return new SSH(user,
                       ssh,
                       Iterables.concat(args, asList("-F", file.getAbsolutePath())),
                       host,
                       inheritOut,
                       inheritErr);
    }
}
