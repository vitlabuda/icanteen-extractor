package cz.vitlabuda.icanteenextractor;

/*
SPDX-License-Identifier: BSD-3-Clause

Copyright (c) 2021 Vít Labuda. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:
 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
    disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other materials provided with the distribution.
 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
    products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.



--- Jsoup ---
SPDX-License-Identifier: MIT

Copyright (c) 2009 - 2021 Jonathan Hedley (https://jsoup.org/)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The main class used to extract food menus from the iCanteen login page.
 */
public class ICanteenExtractor {
    public static final int LIBRARY_VERSION = 2;
    public static final String LIBRARY_VERSION_STRING = "2.0";

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final FoodMenuParserBase[] foodMenuParsers = new FoodMenuParserBase[] {
        new NewExtendedMenuParser(),
        new SimpleMenuParser(),
        new OldExtendedMenuParser()
    };

    private String userAgent = null;
    private int timeoutMilliseconds = -1;

    /**
     * Instantiates the ICanteenExtractor class.
     */
    public ICanteenExtractor() {}

    /**
     * Sets the User-Agent HTTP header sent to the iCanteen server.
     *
     * @param userAgent The new User-Agent.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Sets the timeout of the iCanteen server connection.
     *
     * @param timeoutMilliseconds The new timeout in milliseconds.
     */
    public void setTimeoutMilliseconds(int timeoutMilliseconds) {
        this.timeoutMilliseconds = timeoutMilliseconds;
    }

    /**
     * Fetches and parses the food menu from an iCanteen login page.
     * The login page's URL should look like this: https://strav.nasejidelna.cz/0051/login
     *
     * @param urlString The URL to fetch the food menu from.
     * @return The parsed food menu.
     * @throws ICanteenExtractorException If anything goes wrong while fetching and parsing the food menu.
     */
    public FoodMenu extract(String urlString) throws ICanteenExtractorException {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new ICanteenExtractorException("Failed to parse the supplied URL string!", e);
        }

        return extract(url);
    }

    /**
     * Fetches and parses the food menu from an iCanteen login page.
     * The login page's URL should look like this: https://strav.nasejidelna.cz/0051/login
     *
     * @param url The URL to fetch the food menu from.
     * @return The parsed food menu.
     * @throws ICanteenExtractorException If anything goes wrong while fetching and parsing the food menu.
     */
    public FoodMenu extract(URL url) throws ICanteenExtractorException {
        String protocol = url.getProtocol();
        if(!protocol.equals("https") && !protocol.equals("http"))
            throw new ICanteenExtractorException("The supplied URL has an invalid protocol!");

        String html = fetchHTMLFromURL(url);

        FoodMenu foodMenu = parseHTMLToFoodMenu(html);
        verifyParsedFoodMenu(foodMenu);

        return foodMenu;
    }

    private String fetchHTMLFromURL(URL url) throws ICanteenExtractorException {
        StringBuilder htmlBuilder = new StringBuilder();

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            connection.setDoInput(true);

            if(userAgent != null)
                connection.setRequestProperty("User-Agent", userAgent);

            if(timeoutMilliseconds > 0) {
                connection.setConnectTimeout(timeoutMilliseconds);
                connection.setReadTimeout(timeoutMilliseconds);
            }


            int responseCode = connection.getResponseCode();
            if (responseCode != 200)
                throw new ICanteenExtractorException("The iCanteen server has reported HTTP error " + responseCode + "!");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String temp;
                while ((temp = reader.readLine()) != null) {
                    htmlBuilder.append(temp);
                    htmlBuilder.append(LINE_SEPARATOR);
                }
            }

        } catch (SSLException e) {
            throw new ICanteenExtractorException("Failed to establish a secure connection to the iCanteen server!", e);
        } catch (SocketTimeoutException e) {
            throw new ICanteenExtractorException("The connection to the iCanteen server has timed out!", e);
        } catch (IOException e) {
            throw new ICanteenExtractorException("Failed to connect to the iCanteen server!", e);
        } finally {
            if(connection != null)
                connection.disconnect();
        }

        return htmlBuilder.toString();
    }

    private FoodMenu parseHTMLToFoodMenu(String html) throws ICanteenExtractorException {
        Throwable lastExceptionOrError = null;

        // The parsers are registered & instantiated in the foodMenuParsers instance variable.
        for(FoodMenuParserBase foodMenuParser : foodMenuParsers) {
            try {
                return foodMenuParser.parseHTMLToFoodMenu(html);
            } catch (Exception | Error e) {
                lastExceptionOrError = e;
            }
        }

        throw new ICanteenExtractorException("An error occurred while parsing the HTML webpage!", lastExceptionOrError);
    }

    private void verifyParsedFoodMenu(FoodMenu foodMenu) throws ICanteenExtractorException {
        Iterator<FoodMenu.Day> dayIterator = foodMenu.getDays().iterator();
        while(dayIterator.hasNext()) {
            ArrayList<FoodMenu.Dish> currentDishes = dayIterator.next().getDishes();

            currentDishes.removeIf(currentDish -> currentDish.getDishName().isEmpty() || currentDish.getDishDescription().isEmpty());

            if(currentDishes.isEmpty())
                dayIterator.remove();
        }

        // This can happen, if the canteen hasn't published any food menu - it doesn't have to be an error, if the program using the library wishes so
        // -> the reason why a special exception is thrown.
        if(foodMenu.getDays().isEmpty())
            throw new ICanteenExtractorException.NoFoodMenuException("No food menu was present on the supplied URL!"); // NoFoodMenuException extends ICanteenExtractorException
    }
}
