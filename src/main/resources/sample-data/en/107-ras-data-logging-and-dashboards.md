# RAS basics — Data logging and dashboards

## What data a RAS should log
A RAS should log every online sensor reading together with daily operator records such as feed given, mortalities,
water exchange, chemical dosing and maintenance work. Sensor values are usually stored every 1–5 minutes, while
manual records are entered once or several times a day. Keeping both in one system makes it possible to link water
quality to fish performance.

## Logging interval and data storage
A logging interval of one minute is common for dissolved oxygen and temperature, while slower parameters such as
salinity can be stored every 5–15 minutes. One sensor logged every minute produces about 525,000 values per year,
which is a small amount for a modern database. Farms usually keep raw data for at least one year and summary data
for the life of the farm.

## Controllers, PLCs and data loggers
Sensor signals in a RAS are usually collected by a PLC, a dedicated controller or a data logger, often through 4–20
mA signals, Modbus or similar industrial protocols. The controller passes the values to a SCADA system or database
on a local server or in the cloud. A local copy of the data keeps the farm working if the internet connection
fails.

## Time stamps and clock accuracy
Every logged value needs an accurate time stamp, because operators compare events across many sensors and tanks.
All controllers and servers should synchronise their clocks automatically, for example with a network time server,
and store times in a single time zone such as UTC. Wrong clocks make it impossible to tell whether a pump failure
came before or after an oxygen drop.

## What a RAS dashboard shows
A RAS dashboard is a screen that shows the current state of the farm at a glance, usually with one tile per tank
showing oxygen, temperature, pH and flow. Values within limits are typically shown in green, warnings in yellow and
alarms in red. A good dashboard lets an operator see within a few seconds whether any tank needs attention.

## Trend charts
Trend charts plot sensor values over time, such as the last 24 hours, 7 days or 30 days, and they are the main tool
for finding slow changes in a RAS. Plotting oxygen, feeding and temperature on the same chart shows how fish
respond to each meal. Alarm limits drawn on the chart make it easy to see how close values came to danger.

## Manual records and digital forms
Manual readings such as photometer results for TAN, nitrite and alkalinity should be entered into the same system
as the sensor data, not kept only on paper. Digital forms on a tablet or phone can check that values are in a
sensible range before they are saved. Each record should show who entered it and when.

## Key performance indicators from logged data
Logged RAS data can be turned into key performance indicators such as feed conversion ratio, specific growth rate,
mortality percentage, water use per kilogram of fish and energy use per kilogram of fish. These indicators are
usually reported per tank and per batch each week or month. Comparing indicators between batches shows whether
changes on the farm really helped.

## Remote access to RAS data
Remote access lets managers and on-call staff see the RAS dashboard from a phone or computer outside the farm.
Remote access must be protected with strong passwords, personal user accounts and encrypted connections, because an
attacker who can change setpoints could kill the fish. Many farms allow remote viewing for all staff but limit
remote control to a few trained people.

## Data quality and sensor gaps
Logged data is only useful if it is correct, so a RAS data system should flag missing values, frozen readings and
values outside the physical range of a sensor. Calibration and cleaning events should be logged too, so sudden
jumps in a trend can be explained. Bad data that is not marked can lead to wrong decisions and false conclusions.
