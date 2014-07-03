package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@MessageMapping({"stamp/", "/print/double/"})
public class GreetingController {

    Map<String, Long> lastAccess = new HashMap<String, Long>();

    @Autowired
    private SimpMessagingTemplate template;

    public GreetingController() {
        System.out.println("init");
    }

    @MessageMapping({"/hello{name}n"})
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message,
                             @DestinationVariable String name,
                             Principal user)
            throws Exception {
        lastAccess.put(user.getName(), System.currentTimeMillis());
        if ("error".equals(name)) {
            throw new IllegalArgumentException("Name 'error' is not allowed!");
        }
        return new Greeting("Hello, " + message.getName() + "! (channel: " + name + ")");
    }

    @RequestMapping(value = "/greetings/{name}", method = RequestMethod.GET)
    public String greet(String greeting,
                        @PathVariable String name) {
        template.convertAndSend("/topic/greetings", new Greeting("name: " + greeting));
        return "";
    }

    @MessageMapping({"/goodbye/{name}/"})
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message,
                             @DestinationVariable String name) {
        return new Greeting("Goodbye, " + message.getName() + "! (" + name + ")");
    }

    @MessageMapping({"ping:{port}/",""})
    public Greeting ping(@DestinationVariable int port) {
        return new Greeting(port % 2 == 0 ? "hit!" : "miss!");
    }

    @MessageMapping
    public Greeting head() {
        return new Greeting("Yes, sir!");
    }

    @SubscribeMapping("/t0pic/greetings")
    public Greeting reply() {
        return new Greeting("Please type your name");
    }

    @Scheduled(fixedDelay = 1000)
    public void remind() {
        for (Map.Entry<String, Long> entry : lastAccess.entrySet()) {
            if (System.currentTimeMillis() - entry.getValue() > 10000) {
                String user = entry.getKey();
                template.convertAndSendToUser(user, "/topic/greetings",
                        new Greeting("You've been inactive for " + (System.currentTimeMillis() - entry.getValue()) / 1000 +
                                " seconds"));
                lastAccess.remove(user);
            }
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void ads() {
        template.convertAndSend("/topic/greetings", new Greeting("Only today knife for 5.99$!"));
    }

    @MessageExceptionHandler
   	@SendToUser("/topic/errors")
   	public Greeting handleException(Throwable exception) {
   		return new Greeting(exception.getMessage());
   	}
}
