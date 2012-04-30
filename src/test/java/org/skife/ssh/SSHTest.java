package org.skife.ssh;

import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class SSHTest
{
    @Test
    public void testFoo() throws Exception
    {
        SSH ssh = SSH.toHost("localhost")
                     .withUserKnownHostsFile(new File("/dev/null"))
                     .withStrictHostKeyChecking(false);
        ProcessResult pr = ssh.exec("echo 'hello world'");
        String out = pr.getStdoutString();
        assertThat(out).isEqualTo("hello world\n");
    }
}
