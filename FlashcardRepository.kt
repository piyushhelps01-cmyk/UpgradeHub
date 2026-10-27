package com.example.data.repository

import com.example.data.local.FlashcardDao
import com.example.data.local.FlashcardDeckEntity
import com.example.data.local.FlashcardItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FlashcardRepository(private val dao: FlashcardDao) {

    fun getAllDecks(): Flow<List<FlashcardDeckEntity>> = dao.getAllDecks()

    fun getCardsForDeck(deckId: Long): Flow<List<FlashcardItemEntity>> = dao.getCardsForDeck(deckId)

    fun getAllCards(): Flow<List<FlashcardItemEntity>> = dao.getAllCards()

    suspend fun createDeck(
        title: String,
        subject: String = "Physics",
        description: String = "",
        colorHex: Long = 0xFF6574FF
    ): Long = withContext(Dispatchers.IO) {
        dao.insertDeck(
            FlashcardDeckEntity(
                title = title.trim(),
                subject = subject,
                description = description.trim(),
                colorHex = colorHex
            )
        )
    }

    suspend fun updateDeck(deck: FlashcardDeckEntity) = withContext(Dispatchers.IO) {
        dao.updateDeck(deck)
    }

    suspend fun deleteDeck(deckId: Long) = withContext(Dispatchers.IO) {
        dao.deleteCardsForDeck(deckId)
        dao.deleteDeckById(deckId)
    }

    suspend fun addCard(
        deckId: Long,
        front: String,
        back: String,
        tag: String = "Formula"
    ): Long = withContext(Dispatchers.IO) {
        dao.insertCard(
            FlashcardItemEntity(
                deckId = deckId,
                front = front.trim(),
                back = back.trim(),
                tag = tag
            )
        )
    }

    suspend fun updateCard(card: FlashcardItemEntity) = withContext(Dispatchers.IO) {
        dao.updateCard(card)
    }

    suspend fun deleteCard(cardId: Long) = withContext(Dispatchers.IO) {
        dao.deleteCardById(cardId)
    }

    suspend fun setCardMastered(cardId: Long, isMastered: Boolean) = withContext(Dispatchers.IO) {
        dao.setCardMastered(cardId, isMastered)
    }

    suspend fun resetDeckMastery(deckId: Long) = withContext(Dispatchers.IO) {
        val cards = dao.getCardsForDeckSync(deckId)
        cards.forEach { card ->
            dao.setCardMastered(card.id, false)
        }
    }

    suspend fun ensureDefaultDecksSeeded() = withContext(Dispatchers.IO) {
        val existing = dao.getAllDecksSync()
        if (existing.isNotEmpty()) return@withContext

        // Seed default high-yield formula & concept decks
        val deck1Id = dao.insertDeck(
            FlashcardDeckEntity(
                title = "Kinematics & Newton's Laws",
                subject = "Physics",
                description = "Core formulas for trajectories, banking, friction & work-energy",
                colorHex = 0xFF6574FF
            )
        )
        dao.insertCards(
            listOf(
                FlashcardItemEntity(
                    deckId = deck1Id,
                    front = "Range of Projectile on Horizontal Ground",
                    back = "R = (u² sin 2θ) / g\nMax Range at θ = 45°: R_max = u² / g\nFor two angles (θ and 90°-θ), Range R is identical.",
                    tag = "Formula"
                ),
                FlashcardItemEntity(
                    deckId = deck1Id,
                    front = "Equation of Trajectory in Terms of Range R",
                    back = "y = x tan θ · (1 - x / R)\nStandard form:\ny = x tan θ - (g x²) / (2 u² cos² θ)",
                    tag = "Formula"
                ),
                FlashcardItemEntity(
                    deckId = deck1Id,
                    front = "Optimum Speed on Banked Road (Frictionless)",
                    back = "tan θ = v² / (R · g)\nv_optimum = √(R · g · tan θ)\nWith friction μ:\nv_max = √[R g (tan θ + μ) / (1 - μ tan θ)]",
                    tag = "Formula"
                ),
                FlashcardItemEntity(
                    deckId = deck1Id,
                    front = "Work-Energy Theorem",
                    back = "W_net = W_conservative + W_non-conservative + W_pseudo = ΔK\nAlways valid for both point masses and rigid bodies in any reference frame.",
                    tag = "Concept"
                ),
                FlashcardItemEntity(
                    deckId = deck1Id,
                    front = "Variable Mass System (Rocket Propulsion)",
                    back = "v = v₀ + v_rel · ln(m₀ / m) - g · t\nThrust force: F_thrust = v_rel · |dm/dt|",
                    tag = "Shortcut"
                )
            )
        )

        val deck2Id = dao.insertDeck(
            FlashcardDeckEntity(
                title = "Organic Reactions & Reagents",
                subject = "Chemistry",
                description = "High-weightage named reactions, mechanisms & identification tests",
                colorHex = 0xFF00C853
            )
        )
        dao.insertCards(
            listOf(
                FlashcardItemEntity(
                    deckId = deck2Id,
                    front = "Aldol Condensation Reagents & Mechanism",
                    back = "Carbonyl compounds having α-H + dilute base (NaOH / Ba(OH)₂).\nProduct: β-hydroxyaldehyde/ketone.\nOn heating (-H₂O): α,β-unsaturated carbonyl.",
                    tag = "Concept"
                ),
                FlashcardItemEntity(
                    deckId = deck2Id,
                    front = "Cannizzaro Reaction Condition",
                    back = "Aldehydes with NO α-H (e.g. HCHO, PhCHO) + 50% Conc. NaOH.\nSelf oxidation-reduction (disproportionation):\n1 mol Alcohol + 1 mol Carboxylate salt.",
                    tag = "Rule"
                ),
                FlashcardItemEntity(
                    deckId = deck2Id,
                    front = "Lucas Reagent Test for 1°, 2°, 3° Alcohols",
                    back = "Reagent: Anhydrous ZnCl₂ + Conc. HCl\n• 3° alcohol: Instant cloudiness/turbidity\n• 2° alcohol: Turbidity within 5 minutes\n• 1° alcohol: Turbidity only upon heating",
                    tag = "Shortcut"
                ),
                FlashcardItemEntity(
                    deckId = deck2Id,
                    front = "Ozonolysis of Alkenes",
                    back = "1. O₃\n2. Zn / H₂O (Reductive) → Aldehydes & Ketones without over-oxidation.\nIf oxidative (H₂O₂): Aldehydes convert into Carboxylic acids.",
                    tag = "Formula"
                )
            )
        )

        val deck3Id = dao.insertDeck(
            FlashcardDeckEntity(
                title = "Calculus & Definite Integrals",
                subject = "Mathematics",
                description = "King's property, Leibniz rule, Wallis formula & standard shortcuts",
                colorHex = 0xFFFFAB00
            )
        )
        dao.insertCards(
            listOf(
                FlashcardItemEntity(
                    deckId = deck3Id,
                    front = "King's Property of Definite Integrals",
                    back = "∫[a to b] f(x) dx = ∫[a to b] f(a + b - x) dx\nSpecial case:\n∫[0 to a] f(x) dx = ∫[0 to a] f(a - x) dx",
                    tag = "Formula"
                ),
                FlashcardItemEntity(
                    deckId = deck3Id,
                    front = "Leibniz Rule for Differentiating Definite Integrals",
                    back = "d/dx [∫[u(x) to v(x)] f(t) dt] = f(v(x)) · v'(x) - f(u(x)) · u'(x)",
                    tag = "Formula"
                ),
                FlashcardItemEntity(
                    deckId = deck3Id,
                    front = "1^∞ Indeterminate Form Shortcut",
                    back = "lim(x→a) [f(x)]^(g(x)) = exp[ lim(x→a) (f(x) - 1) · g(x) ]",
                    tag = "Shortcut"
                ),
                FlashcardItemEntity(
                    deckId = deck3Id,
                    front = "Shortest Distance Between Two Skew Lines",
                    back = "d = |(a₂ - a₁) · (b₁ × b₂)| / |b₁ × b₂|\nIf d = 0, the lines intersect in 3D space.",
                    tag = "Formula"
                )
            )
        )

        val deck4Id = dao.insertDeck(
            FlashcardDeckEntity(
                title = "Modern Physics & Atomic Structure",
                subject = "Physics",
                description = "Photoelectric effect, Bohr model, de Broglie & nuclear half-lives",
                colorHex = 0xFF00E5FF
            )
        )
        dao.insertCards(
            listOf(
                FlashcardItemEntity(
                    deckId = deck4Id,
                    front = "Einstein's Photoelectric Equation",
                    back = "K_max = hν - Φ = hν - hν₀ = e · V_stop\nSlope of V_stop vs ν graph is always (h / e).",
                    tag = "Formula"
                ),
                FlashcardItemEntity(
                    deckId = deck4Id,
                    front = "de Broglie Wavelength of Electron",
                    back = "λ = h / p = h / √(2 · m · q · V)\nFor electron: λ = 12.27 / √V Å\nFor thermal neutron: λ = 0.286 / √E(eV) Å",
                    tag = "Formula"
                ),
                FlashcardItemEntity(
                    deckId = deck4Id,
                    front = "Bohr Orbit Radius & Energy Formulas",
                    back = "r_n = 0.529 · (n² / Z) Å\nE_n = -13.6 · (Z² / n²) eV\nv_n = 2.18 × 10⁶ · (Z / n) m/s",
                    tag = "Formula"
                ),
                FlashcardItemEntity(
                    deckId = deck4Id,
                    front = "Radioactive Decay & Half-Life Law",
                    back = "N(t) = N₀ · e^(-λ · t) = N₀ / (2^n) where n = t / T_half\nT_half = ln 2 / λ = 0.693 / λ\nMean Life τ = 1 / λ = 1.44 · T_half",
                    tag = "Concept"
                )
            )
        )

        // Deck 5: NEET Biology Genetics & Molecular Basis
        val deck5Id = dao.insertDeck(
            FlashcardDeckEntity(
                title = "Genetics & Molecular Basis (NEET)",
                subject = "Biology",
                description = "Mendelian ratios, DNA replication, Lac Operon, transcription & codons",
                colorHex = 0xFF00C853
            )
        )
        dao.insertCards(
            listOf(
                FlashcardItemEntity(
                    deckId = deck5Id,
                    front = "Dihybrid Cross Ratios (Mendel)",
                    back = "• Phenotypic Ratio: 9:3:3:1\n• Genotypic Ratio: 1:2:2:4:1:2:1:2:1\n• Incomplete Dominance (Mirabilis jalapa): 1:2:1 for both phenotype & genotype.",
                    tag = "Shortcut"
                ),
                FlashcardItemEntity(
                    deckId = deck5Id,
                    front = "Lac Operon Structural Genes & Products",
                    back = "• z gene → β-galactosidase (hydrolyzes lactose to glucose + galactose)\n• y gene → Permease (increases membrane permeability to β-galactosides)\n• a gene → Transacetylase\n• Inducer: Allolactose.",
                    tag = "Fact"
                ),
                FlashcardItemEntity(
                    deckId = deck5Id,
                    front = "DNA Replication Enzymes & Direction",
                    back = "• Continuous (leading strand): 5' → 3'\n• Discontinuous (lagging strand): Okazaki fragments joined by DNA ligase\n• Main enzyme: DNA polymerase III (high processivity & 3'→5' proofreading).",
                    tag = "Concept"
                ),
                FlashcardItemEntity(
                    deckId = deck5Id,
                    front = "Start and Stop Codons (Genetic Code)",
                    back = "• Start codon: AUG (codes for Methionine in eukaryotes, Formyl-methionine in prokaryotes)\n• Stop/Nonsense codons: UAA (Ochre), UAG (Amber), UGA (Opal).",
                    tag = "Fact"
                )
            )
        )

        // Deck 6: NEET Human Physiology
        val deck6Id = dao.insertDeck(
            FlashcardDeckEntity(
                title = "Human Physiology & Vital Cycles (NEET)",
                subject = "Biology",
                description = "ECG waves, nephron counter-current, digestion enzymes & hormone feedback",
                colorHex = 0xFF00B0FF
            )
        )
        dao.insertCards(
            listOf(
                FlashcardItemEntity(
                    deckId = deck6Id,
                    front = "Standard ECG Waves & Cardiac Events",
                    back = "• P-wave: Depolarization of atria (atrial contraction)\n• QRS-complex: Depolarization of ventricles (ventricular systole)\n• T-wave: Repolarization of ventricles (ventricular relaxation)\n• End of T-wave marks end of systole.",
                    tag = "Fact"
                ),
                FlashcardItemEntity(
                    deckId = deck6Id,
                    front = "Kidney Counter-Current Mechanism & Osmolarity",
                    back = "• Cortex osmolarity: ~300 mOsmol/L\n• Inner Medulla osmolarity: ~1200 mOsmol/L\n• Descending limb of Henle: Permeable to water, impermeable to electrolytes\n• Ascending limb: Impermeable to water, actively transports electrolytes (NaCl).",
                    tag = "Concept"
                )
            )
        )
    }
}
