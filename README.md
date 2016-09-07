How do i calibrate the color?
---
use polynomial regression
use inverse gamma regression

Result:
----
* orginal
![](https://leanote.com/api/file/getImage?fileId=566cf776ab6441660a0008a0)

* calibtrated
![](https://leanote.com/api/file/getImage?fileId=566cf776ab6441660a00089f)


Problems:
---
>Note: well, this project is just a small demo, if you are expecting an accurate calibration result, a commercial product, such as x-rite, is needed.

not working if your image:
* overexposure
* underexposure
* real low quality
* shadow on the color chart

How to use
---
1. import with Maven.
2. run
3. an imageJ window show up
4. use the line tool, draw a line in the colorchart, which starts at the top-right and ends at the bottom-left.
5. wait,
6. get the result


Inspired By
---
[Chart_White_Balance](http://imagejdocu.tudor.lu/doku.php?id=plugin:color:chart_white_balance:start)
author: Yves Vander Haeghen

Other
---
if you need more informaitions about the implementations and algorigm, plz visit
Calibrate with ColorChart(Chinese)

this project is still underdevelopment, feel free to **STAR ME** XD
