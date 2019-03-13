#!/usr/bin/env groovy

/*
 * facebookScrape takes a list of URLs that should be
 * (re)scraped by Facebook.
 *
 * @param url list of URLs
*/
def call(urls) {
  // Not supported by the Groovy engine in Jenkins.
  // urlList.each {
  for (i = 0; i < urlList.length; i++) {
    def url = urlList[i]
    try {
      URL testURL = new URL(url)
      def encodedURL = java.net.URLEncoder.encode(url, "UTF-8")
      def payload = "id=${encodedURL}&scrape=true"

      echo "Processing ${url}"
      def response = httpRequest(
        url: "https://graph.facebook.com/",
        httpMode: 'POST',
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_FORM',
        requestBody: payload
      )
      echo "Response: ${response.content}"
      if (i+1 != urlList.length) {
        sleep 0.5
      }
    } catch (MalformedURLException e) {
      echo "${url} skipped - malformed URL"
    }
  }
}
