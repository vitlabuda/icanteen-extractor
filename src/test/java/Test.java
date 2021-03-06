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

import cz.vitlabuda.icanteenextractor.FoodMenu;
import cz.vitlabuda.icanteenextractor.ICanteenExtractor;
import cz.vitlabuda.icanteenextractor.ICanteenExtractorException;

import java.text.SimpleDateFormat;

public class Test {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    public static final String[] TEST_URLS = new String[] {
            "https://strav.nasejidelna.cz/demo/login", // iCanteen 2.17.20
            "https://strav.nasejidelna.cz/0117/login", // iCanteen 2.17.15
            "https://strav.nasejidelna.cz/0051/login", // iCanteen 2.17.14
            "http://82.117.143.136:8082/faces/login.jsp", // iCanteen 2.14.18
            "https://intr.dmvm.cz:8443/login", // iCanteen 2.17.15
            "https://strava.sps-chrudim.cz/faces/login.jsp", // iCanteen 2.14.15
    };

    public static final String TEST_USER_AGENT = "ICanteenExtractor-Test/" + ICanteenExtractor.LIBRARY_VERSION_STRING;
    public static final int TEST_TIMEOUT = 2000; // in milliseconds

    public static void main(String[] args) throws ICanteenExtractorException {
        for(String url : TEST_URLS)
            testURL(url);
    }

    private static void testURL(String url) throws ICanteenExtractorException {
        System.out.printf("URL: %s\n", url);


        ICanteenExtractor extractor = new ICanteenExtractor();
        extractor.setUserAgent(TEST_USER_AGENT);
        extractor.setTimeoutMilliseconds(TEST_TIMEOUT);

        FoodMenu foodMenu = extractor.extract(url);

        for(FoodMenu.Day day : foodMenu.getDays()) {
            System.out.println(DATE_FORMATTER.format(day.getDate()));

            for(FoodMenu.Dish dish : day.getDishes())
                System.out.printf("- %s (%s): %s\n", dish.getDishName(), dish.getDishPlace(), dish.getDishDescription());

            System.out.println();
        }


        for(int i = 0; i < 3; i++)
            System.out.println();
    }
}
