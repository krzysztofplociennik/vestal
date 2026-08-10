package com.plociennik.vestal.git.push;

import com.plociennik.vestal.common.VestalException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RepoPushService {

    // todo: to implement
    public void push() {

        // maybe check not needed, at least now
//        if (isRepositoryNotTextBased()) {
//            log.error("[{}] Repository consists of files different than .txt files, operation aborted.", "1341_09082026");
//            throw new VestalException("1341_09082026", "Repository consists of files different than .txt files, push aborted.");
//        }
    }

    private boolean isRepositoryNotTextBased() {
        // todo: to implement
        return false;
    }
}
