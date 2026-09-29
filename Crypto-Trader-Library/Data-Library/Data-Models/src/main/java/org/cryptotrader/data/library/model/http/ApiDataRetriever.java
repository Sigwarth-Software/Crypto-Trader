package org.cryptotrader.data.library.model.http;

//=================================-Imports-==================================
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/** A data retriever for fetching responses from APIs. */
@Getter
@Setter
@Slf4j
public class ApiDataRetriever {
    //============================-Variables-=================================
    private String url;
    private String response;
    //============================-Constants-=================================
    public static final String NO_DATA_ERROR_MESSAGE = "Error: No data received " +
                                                "from API";

    //===========================-Constructors-===============================
    public ApiDataRetriever(@NotNull final String url) {
        this.url = url;
        this.response = "";
        this.fetchResponse();
    }
    //=============================-Methods-==================================

    //---------------------------Fetch-Response-------------------------------
    /** Fetches the response from the API. */
    public void fetchResponse() {
        final StringBuilder responseJson = new StringBuilder();
        HttpsURLConnection urlConnection = null;
        BufferedReader apiReader = null;

        try {
            final URI uri = new URI(this.url);
            urlConnection = (HttpsURLConnection) uri.toURL().openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            final InputStream urlStream = urlConnection.getInputStream();
            final InputStreamReader urlStreamReader = new InputStreamReader(urlStream);
            apiReader = new BufferedReader(urlStreamReader);
            String jsonLine;

            while ((jsonLine = apiReader.readLine()) != null) {
                responseJson.append(jsonLine);
            }
        } catch (@NotNull final URISyntaxException | IOException exception) {
            log.error("Error fetching API data: {}", exception.getMessage());
        } finally {
            if (responseJson.isEmpty()) {
                this.response = NO_DATA_ERROR_MESSAGE;
                log.warn(NO_DATA_ERROR_MESSAGE);
            } else {
                this.response = responseJson.toString();
            }
            shutDownConnections(urlConnection, apiReader);
        }
    }

    //-----------------------Shut-Down-Connections----------------------------
    /**
     * Handles the shutdown of connections.
     *
     * @param urlConnection The URL connection to shut down.
     * @param apiReader The API reader to shut down.
     */
    public static void shutDownConnections(final @Nullable HttpsURLConnection urlConnection,
                                           final @Nullable BufferedReader apiReader) {
        if (apiReader != null) {
            try {
                apiReader.close();
            } catch (@NotNull final IOException ioException) {
                log.error("Error closing API reader: {}", ioException.getMessage());
            }
        }

        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }
    //============================-Overrides-=================================

    //------------------------------Equals------------------------------------

    //------------------------------Hash-Code---------------------------------

    //------------------------------To-String---------------------------------

}
