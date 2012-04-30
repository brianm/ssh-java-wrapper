package org.skife.ssh;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class Muxer implements AutoCloseable
{

    private final LoadingCache<String, Master> masters;

    private final SSH base;
    private final int maxCachedChannels;

    private Muxer(SSH base, int maxCachedChannels)
    {
        this.base = base;
        this.maxCachedChannels = maxCachedChannels;
        masters = CacheBuilder.newBuilder()
                              .maximumSize(maxCachedChannels)
                              .removalListener(new MasterRemover())
                              .build(new MasterLoader());
    }


    public static Muxer withSocketsIn(File dir)
    {
        return new Muxer(SSH.toHost("localhost")
                            .withArgs("-oControlPath=" + new File(dir, "%h-%p-%r").getAbsolutePath()),
                         Integer.MAX_VALUE);
    }

    public Muxer withArgs(String... args)
    {
        return new Muxer(base.withArgs(args), maxCachedChannels);
    }

    public Muxer withUser(String user)
    {
        return new Muxer(base.withUser(user), maxCachedChannels);
    }

    @Override
    public void close() throws Exception
    {
        masters.invalidateAll();
        masters.cleanUp();
    }

    public SSH connect(String host)
    {
        Master m = masters.getUnchecked(host);
        return m.connect();
    }

    private class Master
    {
        private final String  host;
        private       Process process;

        Master(String host)
        {
            this.host = host;
            process = base.withArgs("-M", "-v")
                          .withArgs("-o", "ServerAliveInterval=30")
                          .withHost(host)
                          .dashN();
            InputStream stderr = process.getErrorStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(stderr));

            try {
                StringWriter buf = new StringWriter();
                String line;
                for (line = r.readLine(); line != null; line = r.readLine()) {
                    buf.append(line).append("\n");
                    if (line.contains("Entering interactive session")) {
                        // woot, we're live, let's get out of here!
                        break;
                    }
                }
                if (line == null) {
                    // we hit end of file, meaning process exited, something went wrong!
                    throw new IOException(buf.toString());
                }
            }
            catch (IOException e) {
                throw Throwables.propagate(e);
            }

        }

        public SSH connect()
        {
            try {
                process.exitValue();
                // master has exited, remove it from cache
                masters.invalidate(host);
                return masters.getUnchecked(host).connect();
            }
            catch (Exception e) {
                // le sigh -- master is still running, proceed
                return base.withHost(host);
            }


        }


        void close()
        {
            try {
                process.destroy();
            }
            catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private class MasterLoader extends CacheLoader<String, Master>
    {
        @Override
        public Master load(String key) throws Exception
        {
            return new Master(key);
        }
    }

    private class MasterRemover implements RemovalListener<String, Master>
    {
        @Override
        public void onRemoval(RemovalNotification<String, Master> event)
        {
            event.getValue().close();
        }
    }
}
