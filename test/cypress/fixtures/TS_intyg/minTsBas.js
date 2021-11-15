ts = {
    "intygetAvser": {
        "C1": false,
        "C1E": false,
        "C": true,
        "CE": false,
        "D1": false,
        "D1E": false,
        "D": false,
        "DE": false,
        "Taxi": false,
        "Annat": false
    },

    "identitet": {
        "IDkort": false,
        "FöretagskortTjänstekort": false,
        "Körkort": true,
        "PersonligKännedom": false,
        "Försäkran": false,
        "Pass": false
    },

    "synfunktioner": {
        "synfältsdefekter": false,
        "anamnestiskaUppgifter": false,
        "ögonsjukdom": false,
        "dubbelseende": false,
        "nystagmus": false,
        "synskärpa": {
            "högerÖga": {
                "utanKorrektion": {
                    "ja": true,
                    "värde": "1.1"
                },
                "medKorrektion": {
                    "ja": false,
                    "värde": "1.2"
                },
                "kontaktlinser": false
            },
            "vänsterÖga": {
                "utanKorrektion": {
                    "ja": true,
                    "värde": "0.9"
                },
                "medKorrektion": {
                    "ja": false,
                    "värde": "1.3"
                },
                "kontaktlinser": false
            },
            "binokulärt": {
                "utanKorrektion": {
                    "ja": true,
                    "värde": "1.0"
                },
                "medKorrektion": {
                    "ja": false,
                    "värde": "1.1"
                }
            }
        },
        "styrka": false
    },

    "hörselOchBalans": {
        "balansrubbningar": false,
        "fyraMeter": false 
    },

    "rörelseorganensFunktioner": {
        "rörlighet": {
            "ja": false,
            "text": "Lätt reumatism"
        },
        "nedsättningRörelse": false
    },

    "hjärtOchKärl": {
        "hjärtOchKärlSjukdom": false,
        "hjärnskada": false,
        "stroke": {
            "ja": false,
            "text": "Hypertoni"
        }
    },

    "diabetes": {
        "ja": false,
        "typer": {
            "typ1": true,
            "typ2": {
                "ja":true,
                "behandling": {
                    "kost": true,
                    "Tabletter": true,
                    "Insulin": true
                }
            }
        }
    },

    "neurologiskaSjukdomar": false,

    "epilepsi": {
        "ja": false,
        "text": "Skymningstillstånd har förekommit"
    },

    "njursjukdomar": false,

    "demens": false,

    "sömnOchVakenhetsStörningar": false,

    "alkoholNarkotika": {
        "journaluppgifter": false,
        "vårdinsats": false,
        "provtagning": false,
        "regelbundet": {
            "ja": false,
            "Läkemedel": "Alvedon och Ipreen om vartannat"
        }
    },

    "psykiskaSjukdomar": false,

    "ADHD": {
        "psykiskUtvStörning": false,
        "ADHD": false
    },

    "sjukhusvård": {
        "ja":false,
        "när": "Oktober förra året",
        "klinikNamn": "Allmänna sjukhuset",
        "vad": "Tappade det helt"
    },

    "övrigMedicinering": {
        "ja": false,
        "text": "homeopatiska läkemedel"
    },

    "övrigKommentar": {
        "ja": false,
        "text": "Bra gött i Storfors!"
    },

    "bedömning": {
        "kanInteTaStällning": true,
        "kanTaStällning": {
            "C1": true,
            "C1E": false,
            "C": true,
            "CE": false,
            "D1": true,
            "D1E": false,
            "D": true,
            "DE": false,
            "Taxi": true,
            "Annat": false
        },
        "specialistkompetens": {
            "ja": false,
            "text": "Leva life"
        }
    }
}