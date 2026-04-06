/**
 * Functions that manipulate or generate dates.
 * <br>
 * Most date functions support modifying, setting, offsetting, or rounding a date with respect to one or more
 * time periods.
 * <table border="1">
 *   <caption>The supported time periods</caption>
 *   <tr>
 *     <th>Period</th>
 *     <th>Full Names</th>
 *     <th>Short Name</th>
 *     <th>Range</th>
 *     <th>Notes</th>
 *   </tr>
 *   <tr>
 *       <td> <b>Years</b> </td>
 *       <td> <code>year</code>, <code>years</code> </td>
 *       <td> <code>y</code> </td>
 *       <td>-999,999,999 to 999,999,999</td>
 *       <td></td>
 *   </tr>
 *   <tr>
 *       <td> <b>Months</b> </td>
 *       <td> <code>month</code>, <code>months</code> </td>
 *       <td> <code>M</code> </td>
 *       <td>1 to 12</td>
 *       <td></td>
 *   </tr>
 *   <tr>
 *       <td> <b>Weeks</b> </td>
 *       <td> <code>week</code>, <code>weeks</code> </td>
 *       <td> <code>w</code> </td>
 *       <td>0 to 54</td>
 *       <td>[1]</td>
 *   </tr>
 *   <tr>
 *       <td> <b>Days</b> </td>
 *       <td> <code>day</code>, <code>days</code> </td>
 *       <td> <code>d</code> </td>
 *       <td>1 to 28, 29, 30, or 31</td>
 *       <td></td>
 *   </tr>
 *   <tr>
 *       <td> <b>Hours</b> </td>
 *       <td> <code>hour</code>, <code>hours</code> </td>
 *       <td> <code>h</code> </td>
 *       <td>0 to 23</td>
 *       <td></td>
 *   </tr>
 *   <tr>
 *       <td> <b>Minutes</b> </td>
 *       <td> <code>minute</code>, <code>minutes</code> </td>
 *       <td> <code>m</code> </td>
 *       <td>0 to 59</td>
 *       <td></td>
 *   </tr>
 *   <tr>
 *       <td> <b>Seconds</b> </td>
 *       <td> <code>second</code>, <code>seconds</code> </td>
 *       <td> <code>s</code> </td>
 *       <td>0 to 59</td>
 *       <td></td>
 *   </tr>
 *   <tr>
 *       <td> <b>Milli</b> </td>
 *       <td> <code>milli</code>, <code>millis</code> </td>
 *       <td> <code>I</code> </td>
 *       <td>0 to 999</td>
 *       <td>[2]</td>
 *   </tr>
 *   <tr>
 *       <td> <b>Micro</b> </td>
 *       <td> <code>micro</code>, <code>micros</code> </td>
 *       <td> <code>U</code> </td>
 *       <td>0 to 999</td>
 *       <td>[2]</td>
 *   </tr>
 *   <tr>
 *       <td> <b>Nano</b> </td>
 *       <td> <code>nano</code>, <code>nanos</code> </td>
 *       <td> <code>N</code> </td>
 *       <td>0 to 999</td>
 *       <td>[2]</td>
 *   </tr>
 *   <tr>
 *       <td> <b>Milli of second</b> </td>
 *       <td> <code>milliOfSecond</code>, <code>millisOfSecond</code> </td>
 *       <td> <code>i</code> </td>
 *       <td>0 to 999</td>
 *       <td>[3]</td>
 *   </tr>
 *   <tr>
 *       <td> <b>Micro of second</b> </td>
 *       <td> <code>microOfSecond</code>, <code>microsOfSecond</code> </td>
 *       <td> <code>u</code> </td>
 *       <td>0 to 999,999</td>
 *       <td>[3]</td>
 *   </tr>
 *   <tr>
 *       <td> <b>Nano of second</b> </td>
 *       <td> <code>nanoOfSecond</code>, <code>nanosOfSecond</code> </td>
 *       <td> <code>n</code> </td>
 *       <td>0 to 999,999,999</td>
 *       <td>[3]</td>
 *   </tr>
 * </table>
 * <p>
 * <b>Notes:</b>
 * <ol>
 *   <li>The first day of a week and the first week of a year are set in the EEL context. By default, the first day
 *       of the week is Monday, and there are at least 4 days in the first week, following ISO 8601.</li>
 *   <li>Milli, Micro, and Nano are independent. For example, setting microseconds does not affect milliseconds or
 *       nanoseconds.</li>
 *   <li>Milli of Second, Micro of Second, and Nano of Second update all fractional parts of a second.
 *       For example, setting microseconds to 1,234 sets millis to 1, micros to 234, and nanos to 0.
 *       For adding, subtracting, or snapping, these periods act like their independent counterparts.</li>
 * </ol>
 * <p>
 * Period names are case-sensitive. Time zones can be:
 * <ul>
 *   <li>Fixed offsets – a resolved offset from UTC such as {@code +5}</li>
 *   <li>Geographical regions – a region following specific rules to determine offsets, e.g., {@code Europe/Paris}</li>
 * </ul>
 * <p>
 * Time periods can be snapped to the start of a period with a modifier {@code @<period>}. Snap modifiers
 * always round down.
 * <p>
 * Examples of valid modifiers:
 * <ul>
 *   <li>{@code 7d} : 7 days in the future</li>
 *   <li>{@code -7d} : 7 days in the past</li>
 *   <li>{@code 7days} : 7 days in the future using the full name</li>
 *   <li>{@code @d} : the start of the current day</li>
 * </ul>
 * <p>
 * Offsets are applied in the order passed. Examples:
 * <ul>
 *   <li>{@code date.utc()} – current UTC date-time with nanosecond accuracy</li>
 *   <li>{@code date.utc("@s")} – current UTC date-time with second accuracy</li>
 *   <li>{@code date.utc("1day")} – current UTC date-time plus 1 day</li>
 *   <li>{@code date.utc("@d", "+12h")} – current date at midday UTC</li>
 *   <li>{@code date.utc("@d", "-1_500n")} – 1500 nanoseconds before start of UTC date</li>
 *   <li>{@code date.utc("1d", "@d")} – midnight tomorrow UTC</li>
 *   <li>{@code date.utc("48h", "@d")} – midnight the day after tomorrow UTC</li>
 *   <li>{@code date.start("Europe/Paris")} – instant the EEL context was created in Paris</li>
 *   <li>{@code date.local("+2months")} – current local date-time plus 2 months</li>
 *   <li>{@code date.at("-5", "-1w")} – current date-time in UTC-5 minus 1 week</li>
 *   <li>{@code date.set(${value}, "12h", "0m", "0s", "0n")} – given date with fields set to midday</li>
 *   <li>{@code date.plus(${value}, "15minutes")} – given date plus 15 minutes</li>
 *   <li>{@code date.minus(${value}, "-15minutes")} – given date minus 15 minutes</li>
 *   <li>{@code format.at("Europe/Paris", "d/M/yyyy HH:mm")} – current Paris date and time in given format</li>
 * </ul>
 */
@GroupDescription("date")
package com.github.tymefly.eel.function.date;

import com.github.tymefly.eel.udf.GroupDescription;