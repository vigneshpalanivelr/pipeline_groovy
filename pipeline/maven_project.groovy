def STASH_USERNAME = "STASH_USERNAME"
def STASH_PASSWORD = "STASH_PASSWORD"
node ('master') {
	writeFile(file:	"git-askpass-${BUILD_TAG}", text:"#!/bin/bash\ncase \"\$1\" in \nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) echo \"\${STASH_PASSWORD}\" ;;\nesac")
	sh "chmod a+x "git-askpass-${BUILD_TAG}
}
