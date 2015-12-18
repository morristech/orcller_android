package com.orcller.app.orcller.model;

import com.orcller.app.orcllermodules.model.User;

/**
 * Created by pisces on 11/16/15.
 */
public class Contributor extends User {
    public enum Status {
        None(0),
        Ask(1),
        Invite(2),
        Accept(3);

        private int value;

        private Status(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    };

    public String contributor_id;
    public int contributor_status;
}
