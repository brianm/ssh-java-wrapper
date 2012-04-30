package org.skife.ssh;

import org.junit.Test;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

import static org.fest.assertions.Assertions.assertThat;

public class SSHTest
{
    @Test
    public void testFoo() throws Exception
    {
        SSH ssh = SSH.toHost("localhost")
                     .withConfigFile(new File("src/test/resources/test.conf"))
                     .withArgs("-v")
                     .inheritStandardErr()
                     .withUserKnownHostsFile(new File("/dev/null"))
                     .withStrictHostKeyChecking(false);
        ProcessResult pr = ssh.exec("echo 'hello world'");
        String out = pr.getStdoutString();
        assertThat(out).isEqualTo("hello world\n");
    }
}
