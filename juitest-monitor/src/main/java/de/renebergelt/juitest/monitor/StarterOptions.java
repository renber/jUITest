package de.renebergelt.juitest.monitor;

/**
 * Possible command line options
 */
public class StarterOptions {

    // the hostname/ip to listen on / connect to
    public String host = "127.0.0.1";

    // the port to use
    public int port = 5612;

    public static StarterOptions readFromArgs(String[] args) {

        StarterOptions opt = new StarterOptions();

        for(String arg:args) {
            String larg = arg.toLowerCase();
            if (larg.startsWith("-host=")) {
                opt.host = arg.substring("-host=".length());
            } else
            if (larg.startsWith("-port=")) {
                opt.port = Integer.valueOf(arg.substring("-port=".length()));
            }
        }

        return opt;
    }

}
