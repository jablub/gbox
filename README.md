# gbox


## Prerequisites

lein version
Leiningen 2.9.4 on Java 1.8.0_181 Java HotSpot(TM) 64-Bit Server VM

A dropbox account and an access token.
See: https://www.dropbox.com/lp/developers/reference/oauth-guide #Testing with a generated token

The access token needs to be added to profiles.clj

## Running
 
lein repl

gbox.system=> (go)   : To start the server 

gbox.system=> (halt) : To stop the server 

open browser and go to http://localhost:8888/ide

Run graphql query
```
{
  ls(path: "/") {
    folders {
      name
      path
    }
    files {
      name
    }
  }
}
```

Contents of folders can be nested. NOTE This is currently extremley inefficient. See todo.


```
{
  ls(path: "/") {
    folders {
      name
      path
      files {
        name
      }
      folders {
        name
        path
        files {
          name
        }
        folders {
          name
          path
          files {
            name
          }
          folders {
            name
            path
          }
        }
      }
    }
    files {
      name
    }
  }
}
```

## TODO
- Rewrite to fetch entire dropbox directory recursively
- Validate the ls args - needs to start with `/`
- Display contents of a file
- Tree
- Subscritions to update result on changes

