package Server;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Controller
public class WebsocketController {

    private final Alerts alerts = new Alerts();

    private static final Logger log = LoggerFactory.getLogger(WebsocketController.class);

    @Autowired
    private SimpMessagingTemplate template;

    @Scheduled(fixedRate = 5000)
    public void sendAlerts() throws IOException {
        URL url = new URL("https://apiv2.bitcoinaverage.com/indices/global/ticker/short");
        Set<String> keys = new HashSet<>(alerts.getCurrencyPairs());
        for (String pair : keys) {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("crypto", pair.substring(0, 2));
            connection.setRequestProperty("fiat", pair.substring(3, 5));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }
            bufferedReader.close();

            final JsonParser jp = new JsonParser();
            final JsonElement je = jp.parse(response.toString());
            try {
                BigDecimal last = je.getAsJsonObject().getAsJsonObject(pair).getAsJsonPrimitive("last").getAsBigDecimal();
                AlertMessage msg = new AlertMessage();
                msg.setLimit(alerts.getLimit(pair));
                msg.setPair(pair);
                msg.setTimestamp(System.currentTimeMillis());


                log.info("{} last price = {}", pair, last);
                if (alerts.getLimit(pair).compareTo(last) <= 0) {
                    log.info("Message sent ({})", pair);
                    template.convertAndSend("/alerts", msg);
                }
            }
            catch (NullPointerException ex)
            {
                log.info("Wrong json format.");
                alerts.deleteLimit(pair);
            }
        }
    }

    @PutMapping("/alert")
    @ResponseStatus(HttpStatus.OK)
    public void putAlert(@RequestParam(value = "pair") String pair, @RequestParam(value = "limit") int limit) {
        alerts.setLimit(pair, new BigDecimal(limit));
    }

    @DeleteMapping("/alert")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAlert(@RequestParam(value = "pair") String pair) {
        alerts.deleteLimit(pair);
    }

}

