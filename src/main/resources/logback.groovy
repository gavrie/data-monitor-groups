appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //pattern = "%date{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
        //pattern = "%date{ISO8601} [%thread] %highlight(%-5level) %logger{36} %file:%line - %msg%n"
        //pattern = "%date{ISO8601} [%thread] %highlight(%-5level) %class\\(%file:%line\\) - %msg%n"
        //pattern = "%white(%date{ISO8601}) %cyan([%thread]) %highlight(%-5level) %white(%class{36}.%method\\(%file:%line\\)) - %msg%n"
        pattern = "%date{ISO8601} [%thread] %highlight(%-5level) %class{36}.%method\\(%file:%line\\) - %msg%n"
    }
}

root(INFO, ["STDOUT"])

logger("org.apache.kafka.streams", INFO)
logger("org.apache.kafka", INFO)
logger("io.confluent", ERROR)
logger("io.javalin", ERROR)
logger("org.eclipse.jetty", ERROR)

//logger("com.sunbit.datamonitor", INFO)
