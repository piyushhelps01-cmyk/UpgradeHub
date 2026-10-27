package com.example.data.repository

import com.example.data.model.ChapterExerciseSolution
import com.example.data.model.ChapterSection
import com.example.data.model.FormulaItem
import com.example.data.model.SelfStudyBook
import com.example.data.model.SelfStudyCategory
import com.example.data.model.SelfStudyChapter
import com.example.data.model.SelfStudyMockTest
import com.example.data.model.SelfStudyPyqQuestion
import com.example.data.model.SelfStudyStateBoardItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repository containing complete data catalog imported from SelfStudys
 * (https://play.google.com/store/apps/details?id=selfstudys.com).
 *
 * Covers:
 * 1. NCERT Textbooks, In-Text Exercises, NCERT Solutions & Exemplar
 * 2. JEE Main & Advanced Chapterwise PYQs (2019-2025) with step-by-step solutions
 * 3. NEET (UG) Botany, Zoology, Physics & Chemistry PYQs, NCERT Line-by-Line Questions
 * 4. State Boards (Punjab Board PSEB, UP Board, Bihar Board, CBSE 2025-26 Sample Papers)
 * 5. Smart-Book Revision Notes, Formula Cheat Sheets & Mind Maps
 * 6. Live Mock Tests with Timed CBT Exam Engine & Question Review
 */
class SelfStudyRepository {

    // Saved bookmark IDs for offline access
    private val _bookmarkedItemIds = MutableStateFlow<Set<String>>(
        setOf("ch_phy12_01", "pyq_jee_01", "form_chem_01", "board_pseb_01")
    )
    val bookmarkedItemIds: Flow<Set<String>> = _bookmarkedItemIds.asStateFlow()

    fun toggleBookmark(id: String) {
        val current = _bookmarkedItemIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _bookmarkedItemIds.value = current
    }

    fun isBookmarked(id: String): Boolean {
        return _bookmarkedItemIds.value.contains(id)
    }

    // NCERT Books list
    val allBooks: List<SelfStudyBook> = listOf(
        SelfStudyBook(
            id = "book_phy_12",
            title = "NCERT Physics Part 1 & 2",
            subject = "Physics",
            classLevel = "Class 12",
            medium = "English",
            edition = "2025-26 Latest Rationalised Syllabus",
            author = "NCERT Academic Council",
            totalChapters = 14,
            fileSize = "24.2 MB",
            rating = 4.9f,
            chapters = listOf(
                SelfStudyChapter(
                    id = "ch_phy12_01",
                    bookId = "book_phy_12",
                    chapterNo = 1,
                    title = "Electric Charges and Fields",
                    subject = "Physics",
                    classLevel = "Class 12",
                    summary = "Fundamental electrostatics: Coulomb's Law, Electric Field Lines, Electric Dipole, Flux, and Gauss's Law Applications.",
                    readingSections = listOf(
                        ChapterSection(
                            heading = "1.1 Electric Charges & Conservation",
                            body = "Electric charge is the intrinsic property of elementary particles. Charge is quantised: q = ±ne, where e = 1.602 × 10⁻¹⁹ C. Total charge of an isolated system remains strictly conserved across all physical and chemical processes.",
                            keyFormulas = listOf("q = ± n e", "F = (1 / 4πε₀) · (|q₁ q₂| / r²)", "ε₀ = 8.854 × 10⁻¹² C² N⁻¹ m⁻²")
                        ),
                        ChapterSection(
                            heading = "1.2 Coulomb's Law in Vector Form",
                            body = "Force between two stationary point charges is directly proportional to the product of charges and inversely proportional to the square of separation: F₁₂ = (1/4πε₀)(q₁q₂/r²) r̂₁₂. In a dielectric medium of permittivity ε_r, force reduces by factor K: F_med = F_vac / K.",
                            keyFormulas = listOf("F_med = F_vac / ε_r", "E = F / q₀ = (1 / 4πε₀) · (q / r²) r̂")
                        ),
                        ChapterSection(
                            heading = "1.3 Electric Dipole & Torque",
                            body = "An electric dipole consists of two equal and opposite charges ±q separated by distance 2a. Dipole moment p = q × (2a), pointing from -q to +q. Torque in uniform electric field: τ = p × E. Potential energy: U = -p · E.",
                            keyFormulas = listOf("p = 2 a q", "τ = p × E = p E sin θ", "U = - p · E", "E_axial = (1 / 4πε₀) · (2p / r³)", "E_equatorial = (1 / 4πε₀) · (p / r³)")
                        ),
                        ChapterSection(
                            heading = "1.4 Gauss's Law & Symmetrical Applications",
                            body = "The total electric flux through any closed Gaussian surface equals 1/ε₀ times the net enclosed charge: ∮ E · dA = q_enclosed / ε₀. Applicable for infinite wire: E = λ / (2πε₀r); infinite plane sheet: E = σ / (2ε₀); thin spherical shell: E = (1/4πε₀)(q/r²) outside, and E = 0 inside.",
                            keyFormulas = listOf("Φ = ∮ E · dA = q_enc / ε₀", "E_wire = λ / (2πε₀ r)", "E_sheet = σ / (2ε₀)")
                        )
                    ),
                    exerciseSolutions = listOf(
                        ChapterExerciseSolution(
                            exerciseName = "Exercise 1.1",
                            questionNo = 1,
                            questionText = "What is the force between two small charged spheres having charges of 2 × 10⁻⁷ C and 3 × 10⁻⁷ C placed 30 cm apart in air?",
                            solutionSteps = listOf(
                                "Given: q₁ = 2 × 10⁻⁷ C, q₂ = 3 × 10⁻⁷ C, r = 30 cm = 0.3 m",
                                "Using Coulomb's Law in vacuum: F = (1 / 4πε₀) × (q₁ q₂ / r²)",
                                "Substitute values: F = (9 × 10⁹) × (2 × 10⁻⁷ × 3 × 10⁻⁷) / (0.3)²",
                                "F = (9 × 10⁹ × 6 × 10⁻¹⁴) / 0.09 = (54 × 10⁻⁵) / 0.09 = 6 × 10⁻³ N",
                                "Since both charges are positive, the force is repulsive."
                            ),
                            finalAnswer = "6 × 10⁻³ N (Repulsive)",
                            conceptTip = "Remember to always convert centimeters to meters before squaring the radius!"
                        ),
                        ChapterExerciseSolution(
                            exerciseName = "Exercise 1.1",
                            questionNo = 2,
                            questionText = "The electrostatic force on a small sphere of charge 0.4 μC due to another small sphere of charge -0.8 μC in air is 0.2 N. (a) What is the distance between the two spheres? (b) What is the force on the second sphere due to the first?",
                            solutionSteps = listOf(
                                "Given: q₁ = 0.4 × 10⁻⁶ C, |q₂| = 0.8 × 10⁻⁶ C, F = 0.2 N",
                                "(a) F = (1 / 4πε₀) × (|q₁ q₂| / r²)  =>  r² = (9 × 10⁹) × (0.4 × 10⁻⁶ × 0.8 × 10⁻⁶) / 0.2",
                                "r² = (9 × 10⁹ × 0.32 × 10⁻¹²) / 0.2 = (2.88 × 10⁻³) / 0.2 = 14.4 × 10⁻³ = 1.44 × 10⁻² m²",
                                "r = √(1.44 × 10⁻²) = 0.12 m = 12 cm",
                                "(b) By Newton's Third Law of Motion and Coulomb's mutual interaction, the force on the second sphere is equal in magnitude and attractive."
                            ),
                            finalAnswer = "(a) r = 0.12 m (12 cm); (b) 0.2 N attractive force towards the first sphere.",
                            conceptTip = "Coulomb's Law obeys Newton's third law: F₁₂ = -F₂₁."
                        ),
                        ChapterExerciseSolution(
                            exerciseName = "Exercise 1.2",
                            questionNo = 8,
                            questionText = "Two point charges q_A = 3 μC and q_B = -3 μC are located 20 cm apart in vacuum. What is the electric field at the midpoint O of the line AB joining the two charges?",
                            solutionSteps = listOf(
                                "Distance AB = 20 cm, so AO = OB = r = 10 cm = 0.1 m.",
                                "Electric field due to positive charge q_A at O points along OB: E_A = (9 × 10⁹ × 3 × 10⁻⁶) / (0.1)² = 2.7 × 10⁶ N/C.",
                                "Electric field due to negative charge q_B at O also points towards B (along OB): E_B = (9 × 10⁹ × 3 × 10⁻⁶) / (0.1)² = 2.7 × 10⁶ N/C.",
                                "Since both electric field vectors point in the same direction (along OB), the net field is the vector sum: E_net = E_A + E_B = 2.7 × 10⁶ + 2.7 × 10⁶ = 5.4 × 10⁶ N/C along OB."
                            ),
                            finalAnswer = "E_net = 5.4 × 10⁶ N/C directed along line from A to B",
                            conceptTip = "Fields from unlike charges reinforce each other between the charges."
                        )
                    ),
                    exemplarSolutions = listOf(
                        ChapterExerciseSolution(
                            exerciseName = "NCERT Exemplar",
                            questionNo = 1,
                            questionText = "A hemisphere of radius R is placed in a uniform electric field E parallel to its base. The total electric flux through the curved surface is:",
                            solutionSteps = listOf(
                                "The hemisphere is closed by a circular flat base of area πR².",
                                "Because the electric field is uniform, the net electric flux through any closed surface is zero (∮ E · dA = 0).",
                                "Therefore, Φ_curved + Φ_flat = 0.",
                                "The normal to the flat circular base is perpendicular to the electric field (since field is parallel to base), so Φ_flat = E · A = E A cos(90°) = 0.",
                                "Hence, Φ_curved = 0 as well."
                            ),
                            finalAnswer = "0 (Zero flux)",
                            conceptTip = "Always check the angle between the surface normal and the field vector."
                        )
                    ),
                    quickRevisionPoints = listOf(
                        "Charge is scalar, additive, quantised (q = ne), and invariant under relativistic speeds.",
                        "Coulomb's Law is valid only for stationary point charges.",
                        "Electric field lines never cross each other and emanate from +ve charge to -ve charge.",
                        "Inside a charged conductor in electrostatic equilibrium, E = 0 everywhere.",
                        "Potential energy of a dipole in uniform field is minimum (U = -pE) when aligned parallel (stable equilibrium)."
                    ),
                    formulaSheet = listOf(
                        FormulaItem("Coulomb's Force", "F = (1 / 4πε₀) · (q₁ q₂ / r²)", "Force between two stationary point charges in vacuum", "Newton (N)"),
                        FormulaItem("Electric Dipole Moment", "p = q · 2a", "Vector pointing from negative to positive charge", "Coulomb·meter (C·m)"),
                        FormulaItem("Gauss's Theorem", "Φ = ∮ E · dA = q_enclosed / ε₀", "Flux through closed surface equals enclosed charge divided by permittivity", "N·m²/C"),
                        FormulaItem("Torque on Dipole", "τ = p × E = p E sin θ", "Rotational torque on dipole in uniform external field", "Newton·meter (N·m)")
                    ),
                    pyqs = listOf(
                        SelfStudyPyqQuestion(
                            id = "pyq_jee_01",
                            exam = "JEE Main",
                            year = "2024",
                            subject = "Physics",
                            chapter = "Electric Charges and Fields",
                            questionText = "An electric dipole of moment p is placed at the origin (0,0,0) directed along the z-axis. The electric potential at a point (r, θ) in spherical coordinates is proportional to:",
                            options = listOf("1 / r", "1 / r²", "1 / r³", "log(r)"),
                            correctOptionIndex = 1,
                            stepByStepSolution = "The electrostatic potential of a short electric dipole at distance r and polar angle θ is given by V(r, θ) = (1 / 4πε₀) · (p cos θ / r²). Hence V ∝ 1/r².",
                            difficulty = "Easy",
                            importantTag = "JEE Main 2024 Shift 1"
                        ),
                        SelfStudyPyqQuestion(
                            id = "pyq_jee_02",
                            exam = "JEE Main",
                            year = "2023",
                            subject = "Physics",
                            chapter = "Electric Charges and Fields",
                            questionText = "A uniform electric field E = (3 × 10³ i) N/C is given. What is the flux of this field through a square of side 10 cm on a side whose normal makes a 60° angle with the x-axis?",
                            options = listOf("15 N·m²/C", "30 N·m²/C", "45 N·m²/C", "7.5 N·m²/C"),
                            correctOptionIndex = 0,
                            stepByStepSolution = "Area A = (0.1 m)² = 0.01 m². Electric field E = 3 × 10³ N/C. Angle θ between normal and field is 60°. Flux Φ = E · A = E A cos 60° = (3 × 10³) × 0.01 × (1/2) = 15 N·m²/C.",
                            difficulty = "Easy",
                            importantTag = "JEE Main 2023 Shift 2"
                        )
                    )
                ),
                SelfStudyChapter(
                    id = "ch_phy12_02",
                    bookId = "book_phy_12",
                    chapterNo = 2,
                    title = "Electrostatic Potential and Capacitance",
                    subject = "Physics",
                    classLevel = "Class 12",
                    summary = "Electric Potential, Equipotential surfaces, Potential Energy of charge configurations, Capacitors, Series & Parallel combination, Dielectrics.",
                    readingSections = listOf(
                        ChapterSection(
                            heading = "2.1 Electrostatic Potential Difference",
                            body = "Potential difference V_B - V_A is the work done by an external force in bringing a unit positive charge from A to B against electric force without acceleration: ΔV = W_ext / q. For a point charge: V(r) = (1/4πε₀)(q/r).",
                            keyFormulas = listOf("V = (1 / 4πε₀) · (q / r)", "E = - dV / dr")
                        ),
                        ChapterSection(
                            heading = "2.2 Parallel Plate Capacitor & Dielectrics",
                            body = "Capacitance C = Q / V. For parallel plates of area A separated by distance d in air: C₀ = ε₀A / d. When filled completely with dielectric of constant K: C = K · C₀. Energy stored in capacitor: U = 1/2 CV² = 1/2 Q²/C = 1/2 QV.",
                            keyFormulas = listOf("C₀ = ε₀ A / d", "C = K C₀", "U = 1/2 C V²", "u = 1/2 ε₀ E²")
                        )
                    ),
                    exerciseSolutions = listOf(
                        ChapterExerciseSolution(
                            exerciseName = "Exercise 2.1",
                            questionNo = 1,
                            questionText = "Two charges 5 × 10⁻⁸ C and -3 × 10⁻⁸ C are located 16 cm apart. At what points on the line joining the two charges is the electric potential zero? Take the potential at infinity to be zero.",
                            solutionSteps = listOf(
                                "Let the point P lie at distance x from the charge q₁ = 5 × 10⁻⁸ C.",
                                "Case 1 (P lies between the charges, 0 < x < 16 cm):",
                                "V = (1/4πε₀) [q₁/x + q₂/(16 - x)] = 0",
                                "5 × 10⁻⁸ / x = 3 × 10⁻⁸ / (16 - x) => 5(16 - x) = 3x => 80 - 5x = 3x => 8x = 80 => x = 10 cm.",
                                "Case 2 (P lies outside beyond q₂ where x > 16 cm):",
                                "5/x = 3/(x - 16) => 5(x - 16) = 3x => 2x = 80 => x = 40 cm."
                            ),
                            finalAnswer = "Electric potential is zero at 10 cm and 40 cm from the 5 × 10⁻⁸ C charge on the line joining them.",
                            conceptTip = "Electric potential is a scalar quantity, so simply sum algebraic terms V₁ + V₂ = 0."
                        )
                    ),
                    exemplarSolutions = emptyList(),
                    quickRevisionPoints = listOf(
                        "Equipotential surfaces are always perpendicular to electric field lines.",
                        "No work is done in moving a test charge over an equipotential surface.",
                        "When dielectric slab is inserted with battery connected, V remains constant and Q increases.",
                        "When battery is disconnected before inserting dielectric, Q remains constant and V decreases."
                    ),
                    formulaSheet = listOf(
                        FormulaItem("Capacitance of Parallel Plate", "C = ε₀ A / d", "Capacitance in vacuum", "Farad (F)"),
                        FormulaItem("Energy Density", "u = 1/2 ε₀ E²", "Electrostatic energy stored per unit volume", "J/m³")
                    ),
                    pyqs = listOf(
                        SelfStudyPyqQuestion(
                            id = "pyq_jee_03",
                            exam = "JEE Main",
                            year = "2024",
                            subject = "Physics",
                            chapter = "Electrostatic Potential and Capacitance",
                            questionText = "A capacitor of 20 μF is charged to 500 V and disconnected from battery. It is then connected across an uncharged 30 μF capacitor. The common potential across the combination is:",
                            options = listOf("200 V", "250 V", "300 V", "150 V"),
                            correctOptionIndex = 0,
                            stepByStepSolution = "Total charge Q = C₁ V₁ = (20 μF) × (500 V) = 10,000 μC. Total capacitance in parallel C_total = C₁ + C₂ = 20 + 30 = 50 μF. Common potential V_common = Q / C_total = 10,000 / 50 = 200 V.",
                            difficulty = "Medium",
                            importantTag = "JEE Main 2024 Shift 2"
                        )
                    )
                )
            )
        ),
        SelfStudyBook(
            id = "book_chem_12",
            title = "NCERT Chemistry Part 1 & 2",
            subject = "Chemistry",
            classLevel = "Class 12",
            medium = "English",
            edition = "2025-26 NCERT Rationalised Edition",
            author = "NCERT Chemistry Panel",
            totalChapters = 10,
            fileSize = "28.1 MB",
            rating = 4.9f,
            chapters = listOf(
                SelfStudyChapter(
                    id = "ch_chem12_01",
                    bookId = "book_chem_12",
                    chapterNo = 1,
                    title = "Solutions",
                    subject = "Chemistry",
                    classLevel = "Class 12",
                    summary = "Types of Solutions, Raoult's Law, Henry's Law, Colligative Properties, Van't Hoff Factor, Abnormal Molar Mass.",
                    readingSections = listOf(
                        ChapterSection(
                            heading = "1.1 Henry's Law & Gas Solubility",
                            body = "The solubility of a gas in a liquid is directly proportional to the partial pressure of the gas present above the surface of the liquid or solution: p = K_H · x, where K_H is Henry's Law constant. Higher K_H indicates lower solubility.",
                            keyFormulas = listOf("p = K_H · x", "Molarity = n_solute / V_soln(L)", "Molality = n_solute / W_solvent(kg)")
                        ),
                        ChapterSection(
                            heading = "1.2 Raoult's Law & Colligative Properties",
                            body = "For a solution of volatile liquids: p₁ = x₁ p₁° and p₂ = x₂ p₂°. Colligative properties depend solely on the number of solute particles: Relative Lowering of Vapour Pressure (Δp/p₁° = x₂), Elevation of Boiling Point (ΔT_b = i K_b m), Depression of Freezing Point (ΔT_f = i K_f m), and Osmotic Pressure (Π = i C R T).",
                            keyFormulas = listOf("Δp / p₁° = i · x₂", "ΔT_b = i · K_b · m", "ΔT_f = i · K_f · m", "Π = i · C R T", "i = 1 + (n - 1)α (Dissociation)")
                        )
                    ),
                    exerciseSolutions = listOf(
                        ChapterExerciseSolution(
                            exerciseName = "NCERT Exercise 1.4",
                            questionNo = 4,
                            questionText = "Concentrated nitric acid used in laboratory work is 68% nitric acid by mass in aqueous solution. What should be the molarity of such a sample of the acid if the density of the solution is 1.504 g/mL?",
                            solutionSteps = listOf(
                                "68% by mass means 68 g of HNO₃ in 100 g of solution.",
                                "Molar mass of HNO₃ = 1 + 14 + 3(16) = 63 g/mol.",
                                "Moles of HNO₃ in 100 g solution = 68 / 63 = 1.079 mol.",
                                "Volume of 100 g solution = mass / density = 100 g / 1.504 g/mL = 66.49 mL = 0.06649 L.",
                                "Molarity = moles / Volume(L) = 1.079 / 0.06649 = 16.23 M."
                            ),
                            finalAnswer = "Molarity = 16.23 M (or 16.23 mol/L)",
                            conceptTip = "Direct formula shortcut: Molarity = (10 × % by mass × density) / Molar Mass = (10 × 68 × 1.504) / 63 = 16.23 M."
                        )
                    ),
                    exemplarSolutions = emptyList(),
                    quickRevisionPoints = listOf(
                        "An ideal solution obeys Raoult's law over the entire concentration range with ΔH_mix = 0 and ΔV_mix = 0.",
                        "Positive deviation solutions (e.g. ethanol + acetone) form minimum boiling azeotropes.",
                        "Negative deviation solutions (e.g. chloroform + acetone) form maximum boiling azeotropes.",
                        "Van't Hoff factor i > 1 for dissociation (NaCl i≈2) and i < 1 for association (acetic acid in benzene i≈0.5)."
                    ),
                    formulaSheet = listOf(
                        FormulaItem("Osmotic Pressure", "Π = i · C R T", "Colligative property best suited for polymers and proteins", "Atmosphere / Bar"),
                        FormulaItem("Van't Hoff Factor (Association)", "i = 1 + (1/n - 1)α", "Dimerization or trimerization in non-polar solvents", "Dimensionless")
                    ),
                    pyqs = listOf(
                        SelfStudyPyqQuestion(
                            id = "pyq_neet_01",
                            exam = "NEET",
                            year = "2024",
                            subject = "Chemistry",
                            chapter = "Solutions",
                            questionText = "Which of the following aqueous solutions will have the highest boiling point?",
                            options = listOf("0.1 M BaCl₂", "0.1 M NaCl", "0.1 M Glucose", "0.1 M Al₂(SO₄)₃"),
                            correctOptionIndex = 3,
                            stepByStepSolution = "Elevation in boiling point ΔT_b = i K_b m. For equal molarities (0.1 M): Glucose i = 1; NaCl i = 2; BaCl₂ i = 3; Al₂(SO₄)₃ dissociates into 2 Al³⁺ + 3 SO₄²⁻ giving i = 5. Highest i corresponds to maximum ΔT_b and highest boiling point.",
                            difficulty = "Easy",
                            importantTag = "NEET 2024 High-Yield"
                        )
                    )
                )
            )
        ),
        SelfStudyBook(
            id = "book_math_12",
            title = "NCERT Mathematics Part 1 & 2",
            subject = "Mathematics",
            classLevel = "Class 12",
            medium = "English",
            edition = "2025-26 NCERT Syllabus",
            author = "NCERT Mathematics Editorial Board",
            totalChapters = 13,
            fileSize = "22.6 MB",
            rating = 4.9f,
            chapters = listOf(
                SelfStudyChapter(
                    id = "ch_math12_01",
                    bookId = "book_math_12",
                    chapterNo = 1,
                    title = "Relations and Functions",
                    subject = "Mathematics",
                    classLevel = "Class 12",
                    summary = "Types of Relations (Reflexive, Symmetric, Transitive, Equivalence), One-One (Injective), Onto (Surjective), Bijective Functions.",
                    readingSections = listOf(
                        ChapterSection(
                            heading = "1.1 Equivalence Relations",
                            body = "A relation R in set A is: (1) Reflexive if (a, a) ∈ R for all a ∈ A; (2) Symmetric if (a, b) ∈ R implies (b, a) ∈ R; (3) Transitive if (a, b) ∈ R and (b, c) ∈ R implies (a, c) ∈ R. R is an Equivalence Relation if it satisfies all three conditions.",
                            keyFormulas = listOf("Reflexive: (a, a) ∈ R", "Symmetric: (a,b) ∈ R ⇒ (b,a) ∈ R", "Transitive: (a,b) & (b,c) ∈ R ⇒ (a,c) ∈ R")
                        )
                    ),
                    exerciseSolutions = listOf(
                        ChapterExerciseSolution(
                            exerciseName = "Exercise 1.1",
                            questionNo = 1,
                            questionText = "Determine whether the relation R in the set A = {1, 2, 3, ..., 13, 14} defined as R = {(x, y) : 3x - y = 0} is reflexive, symmetric and transitive.",
                            solutionSteps = listOf(
                                "Condition: y = 3x where x, y ∈ {1, 2, ..., 14}.",
                                "R = {(1, 3), (2, 6), (3, 9), (4, 12)}.",
                                "(i) Reflexive: (1, 1) ∉ R since 3(1) - 1 = 2 ≠ 0. Hence R is not reflexive.",
                                "(ii) Symmetric: (1, 3) ∈ R, but (3, 1) ∉ R since 3(3) - 1 = 8 ≠ 0. Hence R is not symmetric.",
                                "(iii) Transitive: (1, 3) ∈ R and (3, 9) ∈ R, but (1, 9) ∉ R since 3(1) - 9 = -6 ≠ 0. Hence R is not transitive."
                            ),
                            finalAnswer = "R is neither reflexive, nor symmetric, nor transitive.",
                            conceptTip = "Always test explicit counterexamples from the relation domain."
                        )
                    ),
                    exemplarSolutions = emptyList(),
                    quickRevisionPoints = listOf(
                        "Number of relations on set with n elements = 2^(n²).",
                        "Number of reflexive relations = 2^(n² - n).",
                        "Function f: A → B is Bijective if and only if it is both Injective (one-to-one) and Surjective (onto)."
                    ),
                    formulaSheet = listOf(
                        FormulaItem("Total One-One Functions", "ⁿPₘ", "From set of m elements to set of n elements (n ≥ m)", "Number")
                    ),
                    pyqs = listOf(
                        SelfStudyPyqQuestion(
                            id = "pyq_jee_04",
                            exam = "JEE Main",
                            year = "2024",
                            subject = "Mathematics",
                            chapter = "Relations and Functions",
                            questionText = "Let A = {1, 2, 3, 4, 5}. The number of equivalence relations containing (1, 2) and (2, 3) is:",
                            options = listOf("2", "1", "3", "5"),
                            correctOptionIndex = 0,
                            stepByStepSolution = "If R contains (1,2) and (2,3), by transitivity it must contain (1,3). By symmetry it must contain (2,1), (3,2), (3,1). Including reflexive pairs, we obtain an equivalence relation with equivalence class {1,2,3} and singletons {4}, {5}. The only other equivalence relation is the universal relation A × A. Thus there are exactly 2 such equivalence relations.",
                            difficulty = "Medium",
                            importantTag = "JEE Main 2024 Shift 1"
                        )
                    )
                )
            )
        ),
        SelfStudyBook(
            id = "book_bio_12",
            title = "NCERT Biology Class 12",
            subject = "Biology",
            classLevel = "Class 12",
            medium = "English",
            edition = "2025-26 NCERT Biology",
            author = "NCERT Life Sciences Panel",
            totalChapters = 13,
            fileSize = "32.0 MB",
            rating = 5.0f,
            chapters = listOf(
                SelfStudyChapter(
                    id = "ch_bio12_01",
                    bookId = "book_bio_12",
                    chapterNo = 2,
                    title = "Sexual Reproduction in Flowering Plants",
                    subject = "Biology",
                    classLevel = "Class 12",
                    summary = "Microsporogenesis, Megasporogenesis, Pollination mechanisms, Double Fertilization, Endosperm and Embryo development.",
                    readingSections = listOf(
                        ChapterSection(
                            heading = "2.1 Double Fertilization",
                            body = "Unique to Angiosperms. Pollen tube releases two male gametes into the synergid. One male gamete fuses with egg nucleus (Syngamy) producing diploid Zygote (2n). The other male gamete fuses with two polar nuclei (Triple Fusion) producing triploid Primary Endosperm Nucleus (PEN, 3n).",
                            keyFormulas = listOf("Syngamy: n + n = 2n (Zygote)", "Triple Fusion: n + 2n = 3n (PEN)")
                        )
                    ),
                    exerciseSolutions = listOf(
                        ChapterExerciseSolution(
                            exerciseName = "NCERT Exercise",
                            questionNo = 1,
                            questionText = "What is double fertilization? Describe its significance.",
                            solutionSteps = listOf(
                                "Double fertilization consists of two distinct fusion events in angiosperms:",
                                "1. Syngamy: Fusion of one haploid male gamete with female egg cell to form diploid zygote.",
                                "2. Triple Fusion: Fusion of the second haploid male gamete with two haploid polar nuclei of central cell to form triploid Primary Endosperm Nucleus (PEN).",
                                "Significance: Ensures endosperm formation occurs only after successful fertilization, preventing waste of maternal plant energy."
                            ),
                            finalAnswer = "Syngamy produces embryo; Triple fusion produces nourishing triploid endosperm tissue.",
                            conceptTip = "P. Maheshwari and S.G. Nawaschin were pioneer researchers of double fertilization."
                        )
                    ),
                    exemplarSolutions = emptyList(),
                    quickRevisionPoints = listOf(
                        "Anther is typically bilobed and dithecous, with four microsporangia.",
                        "Tapetum is the innermost layer nourishing developing pollen grains; cells are multinucleated.",
                        "Female gametophyte (Embryo Sac) is 7-celled and 8-nucleated at maturity.",
                        "Wind pollination (Anemophily) requires light, non-sticky pollen and feathery stigmata (e.g. Maize corn cob)."
                    ),
                    formulaSheet = listOf(
                        FormulaItem("Ploidy Level of Angiosperm Tissues", "Zygote (2n), PEN (3n), Synergids (n), Nucellus (2n)", "Fundamental ploidy rules", "Ploidy")
                    ),
                    pyqs = listOf(
                        SelfStudyPyqQuestion(
                            id = "pyq_neet_02",
                            exam = "NEET",
                            year = "2024",
                            subject = "Biology",
                            chapter = "Sexual Reproduction in Flowering Plants",
                            questionText = "What is the ploidy of cells of the perisperm, aleurone layer and endosperm in a typical seed?",
                            options = listOf("2n, 3n, 3n", "2n, 2n, 3n", "n, 2n, 3n", "2n, 3n, 2n"),
                            correctOptionIndex = 0,
                            stepByStepSolution = "Perisperm is persistent nucellus (diploid, 2n). Aleurone layer is the outer protein-rich cell layer of the endosperm in monocots (triploid, 3n). Endosperm in angiosperms is formed by triple fusion (triploid, 3n). Hence ploidy is 2n, 3n, 3n.",
                            difficulty = "Medium",
                            importantTag = "NEET 2024 Diagram MCQ"
                        )
                    )
                )
            )
        )
    )

    // State Boards Catalog (PSEB, UP Board, CBSE, Bihar Board)
    val stateBoardResources: List<SelfStudyStateBoardItem> = listOf(
        SelfStudyStateBoardItem(
            id = "board_pseb_01",
            boardName = "Punjab State Board (PSEB)",
            classLevel = "Class 12",
            subject = "Physics",
            title = "PSEB Class 12 Physics Complete Guide & Model Question Paper",
            resourceType = "Model Question Paper 2025-26",
            summary = "Official Punjab School Education Board blueprint, bilingual Punjabi/English questions, chapter marks weightage, and solved sample papers.",
            contentSections = listOf(
                ChapterSection(
                    heading = "PSEB Board Examination Pattern",
                    body = "Total Theory: 70 Marks, Practical: 20 Marks, Internal Assessment: 10 Marks. Section A contains 16 MCQs (1 mark each), Section B contains 8 short answer questions (2 marks each), Section C contains 8 questions (3 marks each), and Section D contains 3 long questions with internal choice (5 marks each)."
                ),
                ChapterSection(
                    heading = "High Marks Chapters (PSEB Blueprint)",
                    body = "1. Electrostatics & Current Electricity: 16 Marks\n2. Magnetic Effects of Current & Magnetism: 12 Marks\n3. Optics (Ray & Wave): 14 Marks\n4. Dual Nature & Atoms/Nuclei: 12 Marks\n5. Electronic Devices: 8 Marks"
                )
            ),
            downloadSize = "8.4 MB"
        ),
        SelfStudyStateBoardItem(
            id = "board_up_01",
            boardName = "UP Board (UPMSP)",
            classLevel = "Class 12",
            subject = "Chemistry",
            title = "UPMSP कक्षा 12 रसायन विज्ञान एनसीईआरटी सम्पूर्ण हल व मॉडल पेपर",
            resourceType = "Textbook Solutions Guide (Hindi Medium)",
            summary = "उत्तर प्रदेश माध्यमिक शिक्षा परिषद (UP Board) कक्षा 12 रसायन विज्ञान के सभी अध्यायों के विस्तृत हिंदी उत्तर, महत्वपूर्ण नाम अभिक्रियाएं व बहुविकल्पीय प्रश्न।",
            contentSections = listOf(
                ChapterSection(
                    heading = "महत्वपूर्ण नाम अभिक्रियाएं (Name Reactions)",
                    body = "1. वुर्ट्ज-फिटिग अभिक्रिया (Wurtz-Fittig Reaction): ऐरिल हैलाइड तथा ऐल्किल हैलाइड की शुष्क ईथर में सोडियम के साथ क्रिया से ऐल्किल एरीन प्राप्त होता है।\n2. कैनिजारो अभिक्रिया (Cannizzaro Reaction): अल्फा-हाइड्रोजन विहीन ऐल्डिहाइड सांद्र क्षार की उपस्थिति में स्वतः ऑक्सीकरण-अपचयन करते हैं।"
                )
            ),
            downloadSize = "11.2 MB"
        ),
        SelfStudyStateBoardItem(
            id = "board_cbse_01",
            boardName = "CBSE Board",
            classLevel = "Class 12",
            subject = "Mathematics",
            title = "CBSE Class 12 Official Sample Paper 2025-26 with Marking Scheme",
            resourceType = "Official Sample Paper with Step-by-Step Marking Scheme",
            summary = "Central Board of Secondary Education official 80-mark mathematics question paper with section-wise marks allocation and case-study questions.",
            contentSections = listOf(
                ChapterSection(
                    heading = "Section A - 20 MCQs (1 Mark Each)",
                    body = "Questions 1 to 18 are multiple choice questions and questions 19 and 20 are Assertion-Reason based questions.\nSample Q: If A is a square matrix of order 3 such that |adj(A)| = 64, then |A| is equal to:\nSolution: |adj(A)| = |A|^(n-1) = |A|² = 64 => |A| = ±8."
                ),
                ChapterSection(
                    heading = "Section E - Case Study Questions (4 Marks Each)",
                    body = "Case Study on Applications of Derivatives (Optimization of cylindrical water storage tank minimizing surface area) and Vectors (Shortest distance between two aerial drone trajectories)."
                )
            ),
            downloadSize = "6.8 MB"
        )
    )

    // Full CBT Mock Tests
    val mockTests: List<SelfStudyMockTest> = listOf(
        SelfStudyMockTest(
            id = "mock_jee_main_01",
            title = "JEE Main 2025 All-India CBT Full Mock Test (NTA Pattern)",
            exam = "JEE Main",
            subject = "PCM (Physics, Chemistry, Maths)",
            durationMinutes = 180,
            totalMarks = 300,
            markingScheme = "+4 for correct, -1 for incorrect",
            questions = listOf(
                SelfStudyPyqQuestion(
                    id = "mock_q1",
                    exam = "JEE Main",
                    year = "2025 Expected",
                    subject = "Physics",
                    chapter = "Electric Charges and Fields",
                    questionText = "An infinitely long line of charge produces a field of 9 × 10⁴ N/C at a distance of 2 cm. What is the linear charge density λ?",
                    options = listOf("0.1 μC/m", "1.0 μC/m", "10 μC/m", "0.01 μC/m"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Electric field due to an infinite line charge is E = λ / (2πε₀r) = (2kλ) / r.\nGiven: E = 9 × 10⁴ N/C, r = 0.02 m, k = 9 × 10⁹ N·m²/C².\nλ = (E · r) / (2k) = (9 × 10⁴ × 0.02) / (2 × 9 × 10⁹) = (1800) / (18 × 10⁹) = 10⁻⁷ C/m = 0.1 μC/m.",
                    difficulty = "Easy",
                    importantTag = "High Yield"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q2",
                    exam = "JEE Main",
                    year = "2025 Expected",
                    subject = "Chemistry",
                    chapter = "Solutions",
                    questionText = "The Henry's law constant for dissolution of a gas in water is 1.6 × 10⁵ atm. The mole fraction of gas in solution at 2.5 atm partial pressure is:",
                    options = listOf("1.56 × 10⁻⁵", "6.4 × 10⁻⁵", "2.5 × 10⁻⁴", "4.0 × 10⁻⁶"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Henry's Law: p = K_H · x  =>  x = p / K_H = 2.5 atm / (1.6 × 10⁵ atm) = 1.5625 × 10⁻⁵.",
                    difficulty = "Easy",
                    importantTag = "Standard Calculation"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q3",
                    exam = "JEE Main",
                    year = "2025 Expected",
                    subject = "Mathematics",
                    chapter = "Relations and Functions",
                    questionText = "Let R be a relation on the set of natural numbers N defined by a R b if and only if GCD(a, b) = 2. Then R is:",
                    options = listOf("Symmetric but neither reflexive nor transitive", "Equivalence relation", "Reflexive and symmetric only", "Transitive only"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "(1) Reflexive: a R a => GCD(a, a) = a. For a = 4, GCD(4, 4) = 4 ≠ 2. Not reflexive.\n(2) Symmetric: GCD(a, b) = GCD(b, a) = 2. Always symmetric.\n(3) Transitive: Let a = 6, b = 2, c = 10. GCD(6, 2) = 2 and GCD(2, 10) = 2, but GCD(6, 10) = 2. But take a = 6, b = 4, c = 12: GCD(6, 4)=2, GCD(4, 12)=4≠2. Hence not transitive.",
                    difficulty = "Medium",
                    importantTag = "Conceptual Trap"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q4",
                    exam = "JEE Main",
                    year = "2025 Expected",
                    subject = "Physics",
                    chapter = "Electrostatic Potential",
                    questionText = "A parallel plate capacitor is charged and the battery is disconnected. If the distance between the plates is doubled, how does the stored energy change?",
                    options = listOf("Doubles", "Halves", "Remains unchanged", "Quadruples"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "When battery is disconnected, charge Q remains constant. Capacitance C = ε₀A/d. Doubling d halves the capacitance: C' = C/2. Energy U = Q² / (2C). Since C is halved, stored energy doubles: U' = Q² / (2(C/2)) = 2U.",
                    difficulty = "Easy",
                    importantTag = "Core Concept"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q5",
                    exam = "JEE Main",
                    year = "2024 Shift 1",
                    subject = "Chemistry",
                    chapter = "Coordination Compounds",
                    questionText = "The hybridization and magnetic character of the complex ion [Co(NH₃)₆]³⁺ are respectively:",
                    options = listOf("d²sp³, Diamagnetic", "sp³d², Paramagnetic", "d²sp³, Paramagnetic", "sp³d², Diamagnetic"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "In [Co(NH₃)₆]³⁺, Co is in +3 oxidation state (3d⁶). NH₃ acts as a strong field ligand, forcing pairing of electrons: t₂g⁶ eg⁰. The inner 3d orbitals (two), one 4s, and three 4p orbitals hybridize to form d²sp³ octahedral complex. With all paired electrons, it is diamagnetic.",
                    difficulty = "Medium",
                    importantTag = "Coordination Theory"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q6",
                    exam = "JEE Main",
                    year = "2024 Shift 2",
                    subject = "Mathematics",
                    chapter = "Definite Integrals",
                    questionText = "Evaluate the definite integral I = ∫₀^(π/2) (sin³x) / (sin³x + cos³x) dx.",
                    options = listOf("π / 4", "π / 2", "π / 8", "1"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Using King's Property: I = ∫₀^(π/2) (cos³x) / (cos³x + sin³x) dx.\nAdding both equations: 2I = ∫₀^(π/2) 1 dx = π/2.\nTherefore, I = π / 4.",
                    difficulty = "Easy",
                    importantTag = "King's Rule"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q7",
                    exam = "JEE Main",
                    year = "2024 Shift 1",
                    subject = "Physics",
                    chapter = "Motion in a Plane",
                    questionText = "A projectile is thrown such that its maximum height H equals its horizontal range R. What is the angle of projection θ?",
                    options = listOf("tan⁻¹(4)", "tan⁻¹(2)", "45°", "60°"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Maximum height H = (u² sin²θ) / (2g).\nHorizontal range R = (2u² sinθ cosθ) / g.\nEquating H = R gives (sin²θ) / (2) = 2 sinθ cosθ => tanθ = 4 => θ = tan⁻¹(4).",
                    difficulty = "Medium",
                    importantTag = "Kinematics"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q8",
                    exam = "JEE Main",
                    year = "2024 Shift 2",
                    subject = "Chemistry",
                    chapter = "Haloalkanes and Haloarenes",
                    questionText = "Which alkyl halide reacts fastest via SN2 mechanism with sodium iodide in acetone?",
                    options = listOf("1-Bromobutane", "2-Bromobutane", "2-Bromo-2-methylpropane", "Bromobenzene"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "SN2 rate depends inversely on steric crowding at the α-carbon: 1° > 2° > 3°. 1-Bromobutane is primary (1°) and experiences minimum steric hindrance during backside attack.",
                    difficulty = "Easy",
                    importantTag = "Reaction Mechanism"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q9",
                    exam = "JEE Main",
                    year = "2024 Shift 1",
                    subject = "Mathematics",
                    chapter = "Three Dimensional Geometry",
                    questionText = "The shortest distance between lines r = a₁ + λb₁ and r = a₂ + μb₂ is zero. This implies the lines are:",
                    options = listOf("Intersecting or coincident", "Strictly skew", "Parallel and non-intersecting", "Perpendicular and skew"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Shortest distance between two lines is d = |(a₂ - a₁) · (b₁ × b₂)| / |b₁ × b₂|. If d = 0, the lines either intersect at a common point or are coincident.",
                    difficulty = "Medium",
                    importantTag = "3D Lines"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q10",
                    exam = "JEE Main",
                    year = "2023 Shift 1",
                    subject = "Physics",
                    chapter = "Dual Nature of Radiation and Matter",
                    questionText = "When light of frequency 2ν₀ is incident on a photosensitive metal plate of threshold frequency ν₀, maximum kinetic energy of emitted photoelectrons is K. If frequency is doubled to 4ν₀, the new maximum kinetic energy is:",
                    options = listOf("3K", "2K", "4K", "K / 2"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Einstein's Photoelectric Equation: K_max = hν - hν₀.\nCase 1: K = h(2ν₀) - hν₀ = hν₀.\nCase 2: K' = h(4ν₀) - hν₀ = 3hν₀ = 3K.",
                    difficulty = "Easy",
                    importantTag = "Photoelectric"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q11",
                    exam = "JEE Main",
                    year = "2023 Shift 2",
                    subject = "Chemistry",
                    chapter = "Chemical Kinetics",
                    questionText = "For a first-order reaction A → products, 75% of reaction completes in 32 minutes. The half-life (t₁/₂) of this reaction is:",
                    options = listOf("16 minutes", "8 minutes", "24 minutes", "4 minutes"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "For first-order kinetics, t_75% = 2 × t₁/₂.\nTherefore, t₁/₂ = 32 / 2 = 16 minutes.",
                    difficulty = "Easy",
                    importantTag = "First Order Kinetics"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q12",
                    exam = "JEE Main",
                    year = "2023 Shift 1",
                    subject = "Mathematics",
                    chapter = "Limits and Derivatives",
                    questionText = "Evaluate lim (x → 0) [e^(2x) - 1 - 2x] / x².",
                    options = listOf("2", "1", "4", "0"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Using Taylor expansion: e^(2x) = 1 + 2x + (2x)²/2! + O(x³) = 1 + 2x + 2x² + O(x³).\nThen [e^(2x) - 1 - 2x] / x² = [2x² + O(x³)] / x² = 2.",
                    difficulty = "Medium",
                    importantTag = "L'Hopital / Expansion"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q13",
                    exam = "JEE Main",
                    year = "2023 Shift 2",
                    subject = "Physics",
                    chapter = "Thermodynamics",
                    questionText = "An ideal monoatomic gas expands adiabatically from volume V to 8V. If its initial temperature is T, the final temperature is:",
                    options = listOf("T / 4", "T / 2", "T / 8", "T / 16"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "For an ideal monoatomic gas, γ = 5/3.\nAdiabatic relation: T₁ V₁^(γ-1) = T₂ V₂^(γ-1).\nT · V^(2/3) = T₂ · (8V)^(2/3) = T₂ · (4 V^(2/3)).\nTherefore, T₂ = T / 4.",
                    difficulty = "Medium",
                    importantTag = "Adiabatic Process"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q14",
                    exam = "JEE Main",
                    year = "2022 Shift 1",
                    subject = "Chemistry",
                    chapter = "Electrochemistry",
                    questionText = "The standard reduction potential for Cu²⁺/Cu is +0.34 V and for Zn²⁺/Zn is -0.76 V. The standard EMF of the Daniell cell is:",
                    options = listOf("1.10 V", "-1.10 V", "0.42 V", "1.52 V"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "E°_cell = E°_cathode - E°_anode = E°(Cu²⁺/Cu) - E°(Zn²⁺/Zn) = +0.34 - (-0.76) = +1.10 V.",
                    difficulty = "Easy",
                    importantTag = "Cell EMF"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_q15",
                    exam = "JEE Main",
                    year = "2022 Shift 2",
                    subject = "Mathematics",
                    chapter = "Vectors",
                    questionText = "If vectors a = 2i + j - k and b = i - j + 2k, the unit vector perpendicular to both a and b is:",
                    options = listOf("(i - 5j - 3k) / √35", "(i + 5j + 3k) / √35", "(2i - j + k) / √6", "(i - j - k) / √3"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "a × b = |i j k; 2 1 -1; 1 -1 2| = i(2 - 1) - j(4 - (-1)) + k(-2 - 1) = i - 5j - 3k.\nMagnitude = √(1² + (-5)² + (-3)²) = √(1 + 25 + 9) = √35.\nUnit vector = (i - 5j - 3k) / √35.",
                    difficulty = "Medium",
                    importantTag = "Cross Product"
                )
            )
        ),
        SelfStudyMockTest(
            id = "mock_neet_01",
            title = "NEET 2025 All-India Full Mock Test (720 Marks NTA Pattern)",
            exam = "NEET",
            subject = "PCB (Physics, Chemistry, Biology)",
            durationMinutes = 200,
            totalMarks = 720,
            markingScheme = "+4 for correct, -1 for incorrect",
            questions = listOf(
                SelfStudyPyqQuestion(
                    id = "mock_neet_q1",
                    exam = "NEET",
                    year = "2025 Expected",
                    subject = "Biology",
                    chapter = "Sexual Reproduction in Flowering Plants",
                    questionText = "Filiform apparatus is a characteristic feature of which cell in the embryo sac?",
                    options = listOf("Synergids", "Antipodals", "Egg cell", "Central cell"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "The filiform apparatus consists of cellular thickenings of the synergid cells at the micropylar end that guide the entry of the pollen tube into the synergid.",
                    difficulty = "Easy",
                    importantTag = "Direct NCERT Statement"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q2",
                    exam = "NEET",
                    year = "2025 Expected",
                    subject = "Chemistry",
                    chapter = "Solutions",
                    questionText = "Which colligative property is most widely used for the determination of molar masses of macromolecules, proteins and polymers?",
                    options = listOf("Osmotic pressure", "Relative lowering of vapour pressure", "Elevation in boiling point", "Depression in freezing point"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Osmotic pressure measurement is preferred for macromolecules because: (1) magnitude is easily measurable even at room temperature; (2) molarity is used instead of molality; (3) biomolecules are unstable at elevated boiling temperatures.",
                    difficulty = "Easy",
                    importantTag = "NCERT Line by Line"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q3",
                    exam = "NEET",
                    year = "2024 Shift 1",
                    subject = "Biology",
                    chapter = "Cell Cycle and Cell Division",
                    questionText = "During which phase of the cell cycle does DNA replication take place and where do centrioles duplicate in animal cells?",
                    options = listOf("S-phase; Centrioles in cytoplasm", "G1-phase; Centrioles in nucleus", "G2-phase; Centrioles in cytoplasm", "M-phase; Centrioles in nucleolus"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "According to NCERT Chapter 10, DNA synthesis or replication takes place during the S-phase in the nucleus, while the centriole duplicates in the cytoplasm in animal cells.",
                    difficulty = "Easy",
                    importantTag = "NCERT Statement"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q4",
                    exam = "NEET",
                    year = "2024 Shift 2",
                    subject = "Physics",
                    chapter = "Dual Nature of Radiation",
                    questionText = "An electron is accelerated through a potential difference of 100 V. Its de Broglie wavelength is approximately:",
                    options = listOf("1.227 Å", "0.123 Å", "12.27 Å", "0.012 Å"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "de Broglie wavelength for an electron accelerated through potential V is λ = 12.27 / √V Å. For V = 100 V, λ = 12.27 / 10 = 1.227 Å.",
                    difficulty = "Easy",
                    importantTag = "Standard Formula"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q5",
                    exam = "NEET",
                    year = "2024 Shift 1",
                    subject = "Chemistry",
                    chapter = "Chemical Bonding",
                    questionText = "Which of the following molecules has a zero dipole moment due to symmetric geometry?",
                    options = listOf("Boron trifluoride (BF₃)", "Ammonia (NH₃)", "Water (H₂O)", "Sulfur dioxide (SO₂)"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "BF₃ has a trigonal planar geometry with 120° bond angles. The vector sum of the three B-F bond dipoles cancels out completely, resulting in net μ = 0.",
                    difficulty = "Easy",
                    importantTag = "Dipole Moment"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q6",
                    exam = "NEET",
                    year = "2024 Shift 2",
                    subject = "Biology",
                    chapter = "Human Physiology - Circulation",
                    questionText = "The natural pacemaker of the human heart that generates maximum action potentials (70-75 per min) is:",
                    options = listOf("Sino-atrial Node (SAN)", "Atrio-ventricular Node (AVN)", "Bundle of His", "Purkinje fibres"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "The SAN generates the maximum number of action potentials (70-75/min) and is responsible for initiating and maintaining the rhythmic contractile activity of the heart.",
                    difficulty = "Easy",
                    importantTag = "Direct NCERT"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q7",
                    exam = "NEET",
                    year = "2023 Shift 1",
                    subject = "Physics",
                    chapter = "Ray Optics and Optical Instruments",
                    questionText = "A convex lens of focal length 20 cm in air is immersed in water (refractive index 4/3). The new focal length in water is (glass index = 1.5):",
                    options = listOf("80 cm", "40 cm", "20 cm", "10 cm"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Lens Maker Formula: 1/f_air = (μ_g - 1)(1/R₁ - 1/R₂) = (1.5 - 1)K = 0.5 K => K = 1/(20 × 0.5) = 1/10.\nIn water: 1/f_w = (μ_g/μ_w - 1)K = (1.5 / (4/3) - 1)(1/10) = (9/8 - 1)/10 = 1/80.\nHence f_w = 80 cm (four times the focal length in air).",
                    difficulty = "Medium",
                    importantTag = "Lens Maker"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q8",
                    exam = "NEET",
                    year = "2023 Shift 2",
                    subject = "Chemistry",
                    chapter = "Equilibrium",
                    questionText = "What is the pH of a 10⁻⁸ M aqueous solution of HCl at 25 °C?",
                    options = listOf("6.96 to 6.98 (slightly acidic)", "8.00 (basic)", "7.00 (neutral)", "1.00 (strongly acidic)"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "In extremely dilute acid (10⁻⁸ M), the H⁺ contribution from water autoionization (10⁻⁷ M) cannot be neglected. Total [H⁺] = 10⁻⁸ + 10⁻⁷ = 1.1 × 10⁻⁷ M. pH = -log(1.1 × 10⁻⁷) = 6.96. An acid solution can never have pH ≥ 7.",
                    difficulty = "Medium",
                    importantTag = "Equilibrium Trap"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q9",
                    exam = "NEET",
                    year = "2023 Shift 1",
                    subject = "Biology",
                    chapter = "Principles of Inheritance and Variation",
                    questionText = "In a dihybrid cross between heterozygous round yellow (RrYy) pea plants, what proportion of offspring will display recombinant phenotypes (round green or wrinkled yellow)?",
                    options = listOf("6 / 16", "9 / 16", "1 / 16", "3 / 16"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Dihybrid ratio: 9 Round Yellow (parental) : 3 Round Green (recombinant) : 3 Wrinkled Yellow (recombinant) : 1 Wrinkled Green (parental). Recombinants = 3/16 + 3/16 = 6/16.",
                    difficulty = "Medium",
                    importantTag = "Genetics Ratio"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q10",
                    exam = "NEET",
                    year = "2023 Shift 2",
                    subject = "Physics",
                    chapter = "Current Electricity",
                    questionText = "In a balanced Wheatstone bridge, the resistances in the four arms are P = 10 Ω, Q = 20 Ω, R = 15 Ω, and S = X Ω. What is the value of X?",
                    options = listOf("30 Ω", "7.5 Ω", "15 Ω", "25 Ω"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "For bridge balance: P / Q = R / S => 10 / 20 = 15 / X => 1/2 = 15/X => X = 30 Ω.",
                    difficulty = "Easy",
                    importantTag = "Wheatstone Bridge"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q11",
                    exam = "NEET",
                    year = "2022 Shift 1",
                    subject = "Chemistry",
                    chapter = "Periodic Classification",
                    questionText = "Which halogen element exhibits the most negative electron gain enthalpy in the periodic table?",
                    options = listOf("Chlorine (Cl)", "Fluorine (F)", "Bromine (Br)", "Iodine (I)"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Chlorine has the highest negative electron gain enthalpy (-349 kJ/mol). Although Fluorine is more electronegative, its compact 2p subshell suffers from strong inter-electronic repulsion when an extra electron is added.",
                    difficulty = "Easy",
                    importantTag = "Periodic Exception"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q12",
                    exam = "NEET",
                    year = "2022 Shift 2",
                    subject = "Biology",
                    chapter = "Biotechnology: Principles and Processes",
                    questionText = "The specific DNA recognition sequence cleaved by restriction endonuclease EcoRI is:",
                    options = listOf("5'-GAATTC-3'", "5'-GGATCC-3'", "5'-AAGCTT-3'", "5'-CTCGAG-3'"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "EcoRI recognizes the palindromic hexanucleotide sequence 5'-GAATTC-3' and cuts between G and A on both opposite strands.",
                    difficulty = "Easy",
                    importantTag = "Biotech Enzymes"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q13",
                    exam = "NEET",
                    year = "2022 Shift 1",
                    subject = "Physics",
                    chapter = "Wave Optics",
                    questionText = "In Young's double slit experiment, if distance between slits is halved and distance to screen is doubled, fringe width β becomes:",
                    options = listOf("4 times", "2 times", "Half", "Remains same"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Fringe width β = (λ D) / d. If D' = 2D and d' = d/2, then β' = (λ × 2D) / (d/2) = 4 (λD / d) = 4β.",
                    difficulty = "Easy",
                    importantTag = "YDSE Formula"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q14",
                    exam = "NEET",
                    year = "2022 Shift 2",
                    subject = "Chemistry",
                    chapter = "Organic Chemistry - Alcohols & Phenols",
                    questionText = "When phenol is treated with chloroform and aqueous sodium hydroxide, salicylaldehyde is formed. This reaction is known as:",
                    options = listOf("Reimer-Tiemann Reaction", "Kolbe's Reaction", "Friedel-Crafts Acylation", "Cannizzaro Reaction"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "Treatment of phenol with CHCl₃ in the presence of NaOH introduces a -CHO group at the ortho position of the benzene ring to yield salicylaldehyde; this is the Reimer-Tiemann reaction.",
                    difficulty = "Easy",
                    importantTag = "Named Reaction"
                ),
                SelfStudyPyqQuestion(
                    id = "mock_neet_q15",
                    exam = "NEET",
                    year = "2022 Shift 1",
                    subject = "Biology",
                    chapter = "Ecology and Environment",
                    questionText = "In an ecological pyramid, which pyramid is ALWAYS upright and can never be inverted?",
                    options = listOf("Pyramid of Energy", "Pyramid of Biomass", "Pyramid of Numbers", "Pyramid of Standing Crop"),
                    correctOptionIndex = 0,
                    stepByStepSolution = "The pyramid of energy is always upright because according to Lindeman's 10% law, energy is inevitably dissipated as heat at each trophic transfer and can never increase as it moves up.",
                    difficulty = "Easy",
                    importantTag = "NCERT Ecology"
                )
            )
        )
    )

    // Master Formula Sheet Catalog
    val masterFormulaList: List<FormulaItem> = listOf(
        FormulaItem("Coulomb's Law", "F = (1 / 4πε₀) · (|q₁ q₂| / r²)", "Force between two charges in vacuum (ε₀ = 8.854 × 10⁻¹² C² N⁻¹ m⁻²)", "Newton (N)"),
        FormulaItem("Electric Field due to Point Charge", "E = (1 / 4πε₀) · (q / r²)", "Electric field magnitude at distance r", "N/C or V/m"),
        FormulaItem("Electric Dipole Potential", "V = (1 / 4πε₀) · (p cos θ / r²)", "Potential of dipole at distance r and angle θ", "Volt (V)"),
        FormulaItem("Parallel Plate Capacitance", "C = (K ε₀ A) / d", "Capacitance with dielectric slab of constant K", "Farad (F)"),
        FormulaItem("Energy in Capacitor", "U = 1/2 C V² = 1/2 Q² / C", "Electrostatic potential energy stored in electric field", "Joule (J)"),
        FormulaItem("Henry's Law", "p = K_H · x", "Solubility of gas in liquid at constant temperature", "Atmosphere"),
        FormulaItem("Raoult's Law (Relative Lowering)", "Δp / p₁° = x₂", "Vapour pressure lowering equals mole fraction of non-volatile solute", "Fraction"),
        FormulaItem("Boiling Point Elevation", "ΔT_b = i · K_b · m", "K_b is ebullioscopic constant (water K_b = 0.52 K kg/mol)", "Kelvin (K)"),
        FormulaItem("Freezing Point Depression", "ΔT_f = i · K_f · m", "K_f is cryoscopic constant (water K_f = 1.86 K kg/mol)", "Kelvin (K)"),
        FormulaItem("Osmotic Pressure", "Π = i · C R T", "Pressure required to prevent osmosis through semipermeable membrane", "Atmosphere / Bar"),
        FormulaItem("Distance Between Skew Lines", "d = |(a₂ - a₁) · (b₁ × b₂)| / |b₁ × b₂|", "Shortest distance between two 3D straight lines in vector form", "Units")
    )
}
