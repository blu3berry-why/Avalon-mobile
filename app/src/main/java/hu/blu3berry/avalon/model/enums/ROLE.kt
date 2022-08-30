package com.example.model.enums

enum class ROLE {
    PERCIVAL {
        override fun isEvil(): Boolean {
            return false
        }
    },
    MERLIN {
        override fun isEvil(): Boolean {
            return false
        }
    },
    SERVANT_OF_ARTHUR {
        override fun isEvil(): Boolean {
            return false
        }
    },
    ARNOLD {
        override fun isEvil(): Boolean {
            return false
        }
    },
    ASSASSIN {
        override fun isEvil(): Boolean {
            return true
        }
    },
    MORGANA {
        override fun isEvil(): Boolean {
            return true
        }
    },
    MORDRED {
        override fun isEvil(): Boolean {
            return true
        }
    },
    OBERON {
        override fun isEvil(): Boolean {
            return true
        }
    },
    MINION_OF_MORDRED { // - one of the needed commas
        override fun isEvil(): Boolean {
            return true
        }

    }


    ;

    abstract fun isEvil(): Boolean
    fun getAllEvil(): List<ROLE> {
        return listOf(ASSASSIN, MORGANA, MORDRED, OBERON, MINION_OF_MORDRED)
    }

    fun getAllGood():List<ROLE>{
        return listOf(PERCIVAL, MERLIN, MINION_OF_MORDRED, ARNOLD)
    }
}