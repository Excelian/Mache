FROM tellisnz/travis-jvm-debug

USER travis
WORKDIR /home/travis/build

RUN /bin/bash -lc "git clone https://github.com/Excelian/Mache.git Excelian/Mache ; \
cd Excelian/Mache ; \
travis compile > ~/build/build.sh && chmod 755 ~/build/build.sh ; "
ADD change-branch.sh /home/travis/build
RUN chown travis:travis /home/travis/build/change-branch.sh

CMD /bin/bash
