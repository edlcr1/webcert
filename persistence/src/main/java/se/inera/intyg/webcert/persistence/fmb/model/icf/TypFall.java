/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.fmb.model.icf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FMB_TYPFALL")
public class TypFall {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TYPFALLSMENING", nullable = false)
    private String typfallsMening;

    @Column(name = "MAXIMALSJUKRIVNINGSTID", nullable = false)
    private int maximalSjukrivningstid;

    protected TypFall() {
    }

    private TypFall(final String typfallsMening, final int maximalSjukrivningstid) {
        this.typfallsMening = typfallsMening;
        this.maximalSjukrivningstid = maximalSjukrivningstid;
    }

    public Long getId() {
        return id;
    }

    public String getTypfallsMening() {
        return typfallsMening;
    }

    public int getMaximalSjukrivningstid() {
        return maximalSjukrivningstid;
    }

    public static final class TypFallBuilder {
        private String typfallsMening;
        private int maximalSjukrivningstid;

        private TypFallBuilder() {
        }

        public static TypFallBuilder aTypFall() {
            return new TypFallBuilder();
        }

        public TypFallBuilder typfallsMening(String typfallsMening) {
            this.typfallsMening = typfallsMening;
            return this;
        }

        public TypFallBuilder maximalSjukrivningstid(int maximalSjukrivningstid) {
            this.maximalSjukrivningstid = maximalSjukrivningstid;
            return this;
        }

        public TypFall build() {
            return new TypFall(typfallsMening, maximalSjukrivningstid);
        }
    }
}