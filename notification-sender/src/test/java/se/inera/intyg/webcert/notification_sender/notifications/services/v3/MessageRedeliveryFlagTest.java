/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MessageRedeliveryFlagTest {

    @Test
    public void lowerAndoutdatedTest() {
        long t0 = System.currentTimeMillis() - 1L;
        MessageRedeliveryFlag.StatusFlag sf = new MessageRedeliveryFlag.StatusFlag();

        sf.lowered(System.currentTimeMillis());

        assertTrue(sf.getSuccessTimestamp() > t0);
        assertTrue(sf.isOutdated(t0));
    }

    @Test
    public void raisedTest() {
        MessageRedeliveryFlag.StatusFlag sf = new MessageRedeliveryFlag.StatusFlag();

        sf.raised();

        assertEquals(0L, sf.getSuccessTimestamp());
    }
}