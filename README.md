# VirtualDisplayDemo
该demo为虚拟投屏
主要用于仪表投屏或者launcher投屏
demo中主要包含了两个模块
## service
1. 创建服务
2. 根据自己的设置display信息查询client创建的display
3. 根据需求，通过windowmanager，向display中绘制对应的元素

## client
1. 连接对应的服务
2. 根据对应的信息创建创建对应的display
然后便可以了，创建完了display以后的内容绘制是由service端绘制的
ps：如果是仪表端的，系统直接将改display传递到仪表盘中，具体如何服务端无关

pps:如果需要提供给其他端接入的话，只需要提供client端即可，service就不要提供，避免要求你整些骚东西


