package example.deploy.hotdeploy.client;

public class ArgumentParser {
    private ArgumentConsumer consumer;
    private String[] args;

    public ArgumentParser(ArgumentConsumer consumer, String[] args) {
        this.consumer = consumer;
        this.args = args;
    }

    public void parse() {
        String parameter = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                if (parameter != null) {
                    consumer.argumentFound(parameter, null);
                }

                parameter = arg.substring(2);

                int eq;

                if ((eq = parameter.indexOf('=')) != -1) {
                    String value = parameter.substring(eq+1);
                    parameter = parameter.substring(0, eq);
                    consumer.argumentFound(parameter, value);

                    parameter = null;
                }
            }
            else if (parameter != null) {
                consumer.argumentFound(parameter, arg);
            }
        }

        if (parameter != null) {
            consumer.argumentFound(parameter, null);
        }
    }

}
