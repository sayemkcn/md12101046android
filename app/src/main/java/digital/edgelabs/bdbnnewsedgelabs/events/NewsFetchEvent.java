package digital.edgelabs.bdbnnewsedgelabs.events;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class NewsFetchEvent {
    private String response;

    public NewsFetchEvent(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
