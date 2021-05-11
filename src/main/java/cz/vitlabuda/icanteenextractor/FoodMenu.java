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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * The class containing the parsed food menu.
 */
public class FoodMenu implements Serializable {
    /**
     * A class representing a single day, containing one or more dishes.
     */
    public static final class Day implements Serializable {
        private final Date date;
        private final ArrayList<Dish> dishes = new ArrayList<>();

        /**
         * Instantiates the FoodMenu.Day class.
         *
         * @param date The date of the day
         */
        public Day(Date date) {
            this.date = date;
        }

        /**
         * Gets the date of the day.
         *
         * @return The date of the day.
         */
        public Date getDate() {
            return date;
        }

        /**
         * Gets the dishes wrapped inside the instance.
         *
         * @return A list of FoodMenu.Dish objects.
         */
        public ArrayList<Dish> getDishes() {
            return dishes;
        }
    }

    /**
     * A class representing a single dish, containing its name and content (description).
     */
    public static final class Dish implements Serializable {
        private final String dishName;
        private final String dishPlace;
        private final String dishDescription;

        /**
         * Instantiates the FoodMenu.Dish class.
         *
         * @param dishName The name of the dish, e.g. "Food 1".
         * @param dishDescription The description of the dish, e.g. "Ham and eggs".
         */
        public Dish(String dishName, String dishPlace, String dishDescription) {
            this.dishName = dishName.trim();
            this.dishPlace = dishPlace.trim();
            this.dishDescription = prettifyDishDescription(dishDescription.trim()).trim();
        }

        private String prettifyDishDescription(String dishDescription) {
            dishDescription = dishDescription.replace("*", "");
            dishDescription = dishDescription.replace(",", ", ");
            dishDescription = dishDescription.replaceAll("\\s+,", ",");
            dishDescription = dishDescription.replaceAll("\\s+", " ");

            return dishDescription;
        }

        /**
         * Gets the name of the dish, e.g. "Food 1".
         *
         * @return The name of the dish.
         */
        public String getDishName() {
            return dishName;
        }

        /**
         * Gets the place of the dish, e.g. "Main canteen".
         *
         * @return The place of the dish. Can be empty.
         */
        public String getDishPlace() {
            return dishPlace;
        }

        /**
         * Gets the description of the dish, e.g. "Ham and eggs".
         *
         * @return The description of the dish.
         */
        public String getDishDescription() {
            return dishDescription;
        }
    }

    private final ArrayList<Day> days = new ArrayList<>();

    /**
     * Instantiates the FoodMenu class.
     */
    public FoodMenu() {}

    /**
     * Gets the days wrapped inside the instance.
     *
     * @return A list of FoodMenu.Day objects.
     */
    public ArrayList<Day> getDays() {
        return days;
    }
}
