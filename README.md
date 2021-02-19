# Services

Library for tools of my site 

Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Then add the dependency
```
	dependencies {
          # all modules
          implementation ("com.github.darkraha.services:services:1.064")
	        # or only concrete module
          # implementation ("com.github.darkraha.services:service-core:1.064")
          # implementation ("com.github.darkraha.services:service-http:1.064")
          # implementation ("com.github.darkraha.services:webcrawler:1.064")
	}
```

## http client
Service interface for http / https requests with methods
  * download() - download string
  * downloadFile() - download file
  * downloadByteArray() - download ByteArray
  * postForm() - post HTML form
  * uploadFile() - upload file (not tested yet)
  * restApixxx() - allows to post object as JSON and receive result as object (Gson library is used)

Built-in implementation wrap OkHttpClient.

```
HttpClientOk.newInstance().downloadFile("https://publicobject.com/helloworld.txt")
    .onProgress {
        val progressData = it.getProgressData()
        println("read ${progressData?.current} total ${progressData?.total}")
    }.onSuccess {
        val file: File = it.getResult()
        ... // do something with file
    }
```

## webcrawler
A service that crawls pages from the specified URL. It uses Jsoup library for parsing page.

On *onProgress* block you can handle each parsed page. Or you can write own plugin like SitemapGen.

```
val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask("https://socode4.com/app/en")
           // .maxLinks(40)
            .build()
            .plugin(
                SitemapGen.Builder()
                 //   .changeFreq(SitemapGen.ChangeFreq.weekly)
                    .build()
            )
            .onProgress {
                println(it.getProgressData()!!.action)
                val doc = it.getProgressData().currentData // 
            }.sync().onFinish {
                val sitemapGen = it.getPlugins()!![SitemapGen.NAME] as SitemapGen
                println("total pages ${sitemapGen.countPages}")
                File("socode-sitemap.xml").writeText(sitemapGen.result, Charset.defaultCharset())
            }
```
