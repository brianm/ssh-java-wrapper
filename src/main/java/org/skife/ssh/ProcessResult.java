package org.skife.ssh;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.Reader;

public class ProcessResult
{
    private final int    exitCode;
    private final byte[] stdout;
    private final byte[] stderr;

    ProcessResult(int exitCode, byte[] stdout, byte[] stderr)
    {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    static ProcessResult run(boolean inheritOut, boolean inheritErr, String... cmd) throws InterruptedException, IOException
    {
        ProcessBuilder pb = new ProcessBuilder().command(cmd);

        if (inheritOut) {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }

        if (inheritErr) {
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        }

        Process p = pb.start();
        int exit = p.waitFor();


        byte[] stdout = ByteStreams.toByteArray(p.getInputStream());
        byte[] stderr = ByteStreams.toByteArray(p.getErrorStream());

        return new ProcessResult(exit, stdout, stderr);
    }

    ProcessResult explodeOnError()
    {
        if (exitCode != 0) {
            throw new RuntimeException(new String(stdout));
        }
        return this;
    }

    public int getExitCode()
    {
        return exitCode;
    }

    public byte[] getStdout()
    {
        return stdout;
    }

    public byte[] getStderr()
    {
        return stderr;
    }

    public InputSupplier<? extends Reader> getStdoutSupplier()
    {
        return CharStreams.newReaderSupplier(new String(stdout));
    }

    public InputSupplier<? extends Reader> getStderrSupplier()
    {
        return CharStreams.newReaderSupplier(new String(stderr));
    }

    public String getStdoutString() throws IOException
    {
        return CharStreams.toString(getStdoutSupplier());
    }

    public String getStderrString() throws IOException
    {
        return CharStreams.toString(getStderrSupplier());
    }

    @Override
    public String toString()
    {
        try {
            return getStdoutString();
        }
        catch (IOException e) {
            return "ProcessResult IOException: " + e.getMessage();
        }
    }
}
