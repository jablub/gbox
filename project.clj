(defproject gbox "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [environ "1.2.0"]
                 [cheshire "5.10.0"]
                 [integrant "0.8.0"]
                 [integrant/repl "0.3.2"]
                 [http-kit "2.5.0"]
                 [com.walmartlabs/lacinia "0.37.0"]
                 [com.walmartlabs/lacinia-pedestal "0.14.0"]
                 ]
  :plugins [[lein-environ "1.2.0"]]
  :repl-options {:init-ns gbox.system}
  )
