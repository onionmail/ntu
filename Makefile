WHERE=/usr/bin
CONFD=/etc/ntu
CFLAGS?=-O2
CC?=gcj
SOURCEDIR=src/org/tramaci/ntu
BUILDDIR=org/tramaci/ntu
ECHO=echo

SOURCES := $(shell find $(SOURCEDIR) -name '*.java')

.PHONY: clean install deinstall help

all: ntu

org: $(SOURCES)
	-rm -R org
	javac --sourcepath $(SOURCEDIR)/*.java -d ./ --classpath=org/tramaci/ntu/ -g:none

ntu.jar: org
	jar cfm ntu.jar MANIFEST.MF org
 
ntu: ntu.jar
	gcj  -o ntu ntu.jar --classpath=org/tramaci/ntu/ --main=org.tramaci.ntu.Main $(CFLAGS) $(CPPFLAGS) $(LDFLAGS)

install: ntu
	mkdir -p ${WHERE}
	mkdir -p ${CONFD}
	cp -f ntu ${WHERE}
	cp -f etc/ntu.conf ${CONFD}
	touch /var/log/ntu.log
clean:
	rm -f *~ ntu
	rm -f ntu.jar
	rm -R org

deinstall:
	rm -f ${WHERE}/ntu
	rm -f ${CONFD}/ntu.conf
	rm -f -R ${CONFD}
	rm -f /var/log/ntu.log

