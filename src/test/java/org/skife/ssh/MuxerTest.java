package org.skife.ssh;

import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class MuxerTest
{
    @Test
    public void testFoo() throws Exception
    {
        File tmp = Files.createTempDir();

        try (Muxer m = Muxer.withSocketsIn(tmp)) {
            SSH ssh = m.connect("localhost")
                       .withArgs("-vv");

            ProcessResult r = ssh.exec("echo 'hello multiplexing world'");
            assertThat(r.getStdoutString()).isEqualTo("hello multiplexing world\n");
            assertThat(r.getStderrString()).contains("mux_client_request_session");
        }
    }
}
