package org.skife.ssh;

public class CommandFailed extends RuntimeException
{
    public CommandFailed(String message)
    {
        super(message);
    }
}
