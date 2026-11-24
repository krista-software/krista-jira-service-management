/*
 * JIRA SERVICE MANAGEMENT Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.devsecops.development.jira.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlManipulator {


    public static final String OL = "ol";
    public static final String LI = "li";
    public static final String BR = "<br>";

    /**
     * Converts all unordered list elements (`<ul>`) in the provided HTML string into textual representations.
     *
     * @param html the HTML string to process
     * @return the processed HTML string with all `<ul>` elements replaced with their textual representations
     */
    public static String handleUnorderedLists(String html) {
        Document doc = Jsoup.parse(html);
        Elements unorderedLists = doc.select("ul");
        for (Element ul : unorderedLists) {
            Elements items = ul.select(LI);
            StringBuilder listText = new StringBuilder();
            for (Element li : items) {
                listText.append("- ").append(li.text()).append(BR);
            }
            ul.html(listText.toString());  // Replace the list HTML with the list text
        }
        return doc.body().html();
    }

    /**
     * Converts all ordered list elements (`<ol>`) in the provided HTML string into textual representations.
     *
     * @param html the HTML string to process
     * @return the processed HTML string with all `<ol>` elements replaced with their textual representations
     */
    public static String handleOrderedList(String html) {
        Document doc = Jsoup.parse(html);
        Elements orderedLists = doc.select(OL);
        for (Element ol : orderedLists) {
            Elements items = ol.select(LI);
            StringBuilder listText = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                Element li = items.get(i);
                listText.append(i + 1).append(". ").append(li.text()).append(BR);
            }
            ol.html(listText.toString());  // Replace the list HTML with the list text
        }
        return doc.body().html();
    }

}
