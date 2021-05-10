# Services

Library for tools of [my site socode4.com](https://socode4.com)

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
	  # implementation ("com.github.darkraha.services:all:<version>")
          # implementation ("com.github.darkraha.services:service-core:<version>")
          # implementation ("com.github.darkraha.services:service-http:<version>")
          # implementation ("com.github.darkraha.services:webcrawler:<version>")
	      # implementation ("com.github.DarkRaha.Services:service-diskcache:<version>")
	      # implementation ("com.github.DarkRaha.Services:service-file-endecode:<version>")
	}
	}
```
Current version 2.1.01

## http client
Service interface for http / https requests with methods
  * download() - download string
  * downloadFile() - download file
  * downloadByteArray() - download ByteArray
  * postForm() - post HTML form
  * uploadFile() - upload file (not tested yet)
  * restApixxx() - allows to post object as JSON and receive result as object (Gson library is used)

Built-in implementation wrap OkHttpClient and use Gson, so you need add dependency on them also.

```
HttpServiceOk.newInstance().downloadFile("https://publicobject.com/helloworld.txt")
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
            .subscribe(
                SitemapGen.Builder()
                 //   .changeFreq(SitemapGen.ChangeFreq.weekly)
                    .build()
            )
            .onProgress {
	        val handlingURi = it.getProgressData()?.currentData as HandlingUri
	        val doc: Document = handlingURi.doc
                println(it.getProgressData()!!.action)
            }.async().onFinish {
                val sitemapGen = it.getPlugin(SitemapGen.NAME) as SitemapGen
                val sm = sitemapGen.result.toString()
                File("socode-sitemap.xml").writeText(sm, Charset.defaultCharset())
            }
```
