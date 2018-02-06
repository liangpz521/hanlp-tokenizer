# 基于[HanLP自然语言处理包](https://github.com/hankcs/HanLP)的elasticsearch分词器

### 功能
- 本分词器使用HanLP提供的维特比分词
- 屏蔽了本地配置用户词典
- 增加同义词索引功能
- 增加远程词典热更新（用户词典，停词典，同义词典）

### 使用
目前支持的es版本为2.3.5和5.6.3，其他版本请修改plugin-descriptor.properties文件的es版本
```
git clone this project
mvn clean install
cp hanlp-tokenizer-0.1.tar.gz /your/es/plugins/
tar -zxvf hanlp-tokenizer-0.1.tar.gz
rm hanlp-tokenizer-0.1.tar.gz
restart es
```
提供三种分词方式
```
hanlp-search nlp分词
hanlp-index 索引分词
hanlp-synonym 同义词索引分词
```
测试
```
POST http://localhost:9200/<index_name>/_analyze?analyzer=hanlp_search&pretty=true
{
	"text": "你好，上外日本文化经济学院的陆晚霞教授正在教授泛读课程"
}
```
得到结果
```
{
    "tokens": [
        {
            "token": "你好",
            "start_offset": 0,
            "end_offset": 2,
            "type": "l",
            "position": 0
        },
        {
            "token": "，",
            "start_offset": 2,
            "end_offset": 3,
            "type": "w",
            "position": 1
        },
        {
            "token": "上外日本文化经济学院",
            "start_offset": 3,
            "end_offset": 13,
            "type": "nt",
            "position": 2
        },
        {
            "token": "的",
            "start_offset": 13,
            "end_offset": 14,
            "type": "uj",
            "position": 3
        },
        {
            "token": "陆晚霞",
            "start_offset": 14,
            "end_offset": 17,
            "type": "nr",
            "position": 4
        },
        {
            "token": "教授",
            "start_offset": 17,
            "end_offset": 19,
            "type": "n",
            "position": 5
        },
        {
            "token": "正在",
            "start_offset": 19,
            "end_offset": 21,
            "type": "d",
            "position": 6
        },
        {
            "token": "教授",
            "start_offset": 21,
            "end_offset": 23,
            "type": "n",
            "position": 7
        },
        {
            "token": "泛读",
            "start_offset": 23,
            "end_offset": 25,
            "type": "v",
            "position": 8
        },
        {
            "token": "课程",
            "start_offset": 25,
            "end_offset": 27,
            "type": "n",
            "position": 9
        }
    ]
}
```
### 配置远程词典
在没有配置远程词典时，hanlp_sysnonym == hanlp_index；使用hanlp内置词典
远程词典照搬[ik分词](https://github.com/medcl/elasticsearch-analysis-ik)
- 配置远程词典配置文件hanlp-hot-update.cfg.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
	<comment>HanLP 扩展配置</comment>
	<!--用户可以在这里配置远程扩展字典 -->
	<entry key="remote_ext_dict">http://your/used/dictionary</entry>
	<!--用户可以在这里配置远程扩展停止词字典-->
	<entry key="remote_ext_stopwords">http://your/stop/dictionary</entry>
	<!--用户可以在这里配置远程同义词字典-->
	<entry key="remote_ext_synonyms">http://your/sysnonym/dictionary</entry>
</properties>
```
- 用户词典格式为 每行一个词和词性 词性可以不写 默认为nz
```
天气 n_tianqi
气温 n_tianqi
气象 n_tianqi
温度 n_tianqi
前天 t_day
后天 t_day
今天 t_day
昨天 t_day
明天 t_day
```
- 停词典格式为 每行一个词
```
但是
何
何以
何况
何处
何时
余外
作为
```
- 同义词典格式为 每行一组同义词
```
人 士 人物 人士 人氏 人选
人类 生人 全人类
人手 人员 人口 人丁 口 食指
劳力 劳动力 工作者
```
- 更新方式
```
分词器每分钟会以HEAD方式访问用户配置的词典接口
从response中获取header：ETag、Last-Modified
若这两个header有一个有变化，则以GET方式访问接口并更新词典
更新时：
    若用户词典中的词存在，则跳过
    若同义词典中的词存在，则合并
```
### 同义词功能参考[word](https://github.com/ysc/word)
### 热更新功能参考[elasticsearch-analysis-ik](https://github.com/medcl/elasticsearch-analysis-ik)
### power by [HanLP](https://github.com/hankcs/HanLP)

 _作者：Eugen 邮箱：745045993@qq.com_ 