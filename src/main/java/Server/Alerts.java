package Server;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

public class Alerts {

    private HashMap<String, BigDecimal> limitsMap;

    public Alerts()
    {
        limitsMap = new HashMap<>();
    }

    public void setLimit(String currencyPair, BigDecimal limit)
    {
        limitsMap.put(currencyPair, limit);
        Logger.getAnonymousLogger().info("Limit for " + currencyPair + " set to " + limit);
    }

    public BigDecimal getLimit(String currencyPair)
    {
        return limitsMap.getOrDefault(currencyPair, BigDecimal.ZERO);
    }

    public void deleteLimit(String currencyPair)
    {
        limitsMap.remove(currencyPair);
        Logger.getAnonymousLogger().info("Limit for " + currencyPair + " removed");
    }

    public Set<String> getCurrencyPairs()
    {
        return limitsMap.keySet();
    }

}
