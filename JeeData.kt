package com.example.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.graphics.Color
import com.example.data.model.ChapterItem
import com.example.data.model.ConnectedFlowNode
import com.example.data.model.FacultyStack

object JeeData {

    val CHAPTERS: List<ChapterItem> = listOf(
        // 12 Physics (Saleem Ahmad Sir)
        ChapterItem(0, "12 Physics", "Electrostatics", "30", "48", "Gauss law, field, potential, capacitor combinations", "HIGH"),
        ChapterItem(1, "12 Physics", "Current Electricity", "15", "24", "Kirchhoff, Wheatstone, cells, power", "HIGH"),
        ChapterItem(2, "12 Physics", "Capacitance", "10", "16", "Combination, dielectric, energy", "HIGH"),
        ChapterItem(3, "12 Physics", "Moving Charges & Magnetism", "flow", "—", "Lorentz force, Biot-Savart, Ampere loop", "HIGH"),
        ChapterItem(4, "12 Physics", "EMI + AC", "flow", "—", "Faraday/Lenz, inductance, phasor, resonance", "HIGH"),
        ChapterItem(5, "12 Physics", "Ray & Wave Optics", "flow", "—", "Lens/mirror, instruments, interference, diffraction", "HIGH"),
        ChapterItem(6, "12 Physics", "Modern Physics & Semiconductors", "flow", "—", "Photoelectric, Bohr, nuclei, diodes, logic gates", "HIGH"),

        // 12 Maths (Sachin Jakhar Sir)
        ChapterItem(7, "12 Maths", "Matrices", "11", "18", "Inverse, systems of equations, rank", "HIGH"),
        ChapterItem(8, "12 Maths", "Determinants", "9", "14", "Properties, area, Cramer's rule", "HIGH"),
        ChapterItem(9, "12 Maths", "Relations & Functions", "22", "35", "Domain/range, composition, inverse, periodicity", "HIGH"),
        ChapterItem(10, "12 Maths", "Inverse Trigonometry", "9", "14", "Principal values, identities, sum series", "MED"),
        ChapterItem(11, "12 Maths", "Limits, Continuity & Diff.", "12", "19", "Standard limits, L'Hopital, differentiability", "HIGH"),
        ChapterItem(12, "12 Maths", "Application of Derivatives (AOD)", "flow", "—", "Monotonicity, extrema, tangent/normal", "HIGH"),
        ChapterItem(13, "12 Maths", "Indefinite & Definite Integration", "flow", "—", "Substitution, parts, King's rule, properties", "HIGH"),
        ChapterItem(14, "12 Maths", "Area Under Curve & DE", "6", "10", "Variable separation, linear DE, area between curves", "MED"),
        ChapterItem(15, "12 Maths", "Vectors & 3D Geometry", "flow", "—", "Dot/cross, line-plane, shortest distance", "HIGH"),
        ChapterItem(16, "12 Maths", "Probability", "8", "13", "Conditional probability, Bayes, distributions", "HIGH"),

        // 12 Chemistry (Faisal Sir / Om Pandey Sir / Pankaj Sir)
        ChapterItem(17, "12 Chemistry", "Solutions", "15", "24", "Molarity, colligative properties, Henry's law", "HIGH"),
        ChapterItem(18, "12 Chemistry", "Electrochemistry", "10", "16", "Nernst equation, conductance, batteries, Faraday", "HIGH"),
        ChapterItem(19, "12 Chemistry", "Chemical Kinetics", "10", "16", "Rate law, order, half-life, Arrhenius equation", "HIGH"),
        ChapterItem(20, "12 Chemistry", "Coordination Compounds", "16", "26", "CFT, isomerism, nomenclature, magnetism", "HIGH"),
        ChapterItem(21, "12 Chemistry", "p-Block & d/f-Block Elements", "flow", "—", "Oxidation trends, reactions, colour, metallurgy", "HIGH"),
        ChapterItem(22, "12 Chemistry", "Optical Isomerism & GOC", "11", "18", "R/S, meso, enantiomers, diastereomers, stability", "HIGH"),
        ChapterItem(23, "12 Chemistry", "Hydrocarbons", "15", "24", "Mechanisms, aromaticity, addition reactions", "HIGH"),
        ChapterItem(24, "12 Chemistry", "Haloalkanes & Haloarenes", "13", "21", "SN1/SN2 mechanisms, EAS, conversions", "HIGH"),
        ChapterItem(25, "12 Chemistry", "Alcohol, Phenol & Ether", "10", "16", "Acidity, Kolbe, Reimer-Tiemann, Williamson", "HIGH"),
        ChapterItem(26, "12 Chemistry", "Aldehydes, Ketones & Acids", "12", "19", "Named reactions, nucleophilic addition, tests", "HIGH"),
        ChapterItem(27, "12 Chemistry", "Amines & Biomolecules", "6", "9", "Basicity, diazonium coupling, NCERT carbohydrate/proteins", "HIGH"),

        // 11 Physics (Restart Route)
        ChapterItem(28, "11 Physics", "Kinematics & Vectors", "flow", "—", "Graphs, projectile motion, relative velocity", "HIGH"),
        ChapterItem(29, "11 Physics", "Newton's Laws & Friction", "flow", "—", "FBD, friction, banking, circular motion", "HIGH"),
        ChapterItem(30, "11 Physics", "Work, Energy & Power", "flow", "—", "Work-energy theorem, power, springs, potential energy", "HIGH"),
        ChapterItem(31, "11 Physics", "Center of Mass & Collision", "flow", "—", "Momentum conservation, collision types, COM", "HIGH"),
        ChapterItem(32, "11 Physics", "Rotational Motion", "flow", "—", "Torque, MOI, rolling, angular momentum conservation", "HIGH"),
        ChapterItem(33, "11 Physics", "Thermodynamics & Heat", "flow", "—", "First law, PV processes, Carnot engine, calorimetry", "HIGH"),
        ChapterItem(34, "11 Physics", "SHM & Waves", "flow", "—", "Spring/pendulum SHM, Doppler effect, standing waves", "HIGH"),

        // 11 Chemistry (Restart / Manzil)
        ChapterItem(35, "11 Chemistry", "Mole Concept", "3", "5", "Stoichiometry, limiting reagent, concentration", "HIGH"),
        ChapterItem(36, "11 Chemistry", "Atomic Structure", "2", "3", "Quantum numbers, Bohr model, de Broglie, spectra", "MED"),
        ChapterItem(37, "11 Chemistry", "Thermodynamics", "3", "5", "Enthalpy, Hess law, entropy, Gibbs energy", "HIGH"),
        ChapterItem(38, "11 Chemistry", "Redox Reactions", "1", "2", "Oxidation numbers, ion-electron balancing", "MED"),
        ChapterItem(39, "11 Chemistry", "Chemical & Ionic Equilibrium", "3", "5", "Kc/Kp, Le Chatelier, pH, buffer, solubility product", "HIGH"),
        ChapterItem(40, "11 Chemistry", "Periodicity & Bonding", "7", "11", "Trends, exceptions, VSEPR, hybridisation, MOT", "HIGH"),
        ChapterItem(41, "11 Chemistry", "GOC & Hydrocarbons", "9", "15", "Inductive, resonance, hyperconjugation, aromaticity", "HIGH"),

        // 11 Maths (Anna Sir One-Shot Route)
        ChapterItem(42, "11 Maths", "Quadratic & Basic Maths", "one-shot", "—", "Roots, parameter range, graphs, logarithms", "HIGH"),
        ChapterItem(43, "11 Maths", "Trigonometric Equations", "one-shot", "—", "Identities, general solutions, transformations", "HIGH"),
        ChapterItem(44, "11 Maths", "Sequence & Series", "one-shot", "—", "AP, GP, AGP, telescopic sums", "HIGH"),
        ChapterItem(45, "11 Maths", "Permutation & Combination", "one-shot", "—", "Counting principles, arrangement, selection", "HIGH"),
        ChapterItem(46, "11 Maths", "Straight Lines & Circles", "one-shot", "—", "Family of lines, distance, tangent, chord equations", "HIGH"),
        ChapterItem(47, "11 Maths", "Conic Sections", "one-shot", "—", "Parabola, ellipse, hyperbola standard forms", "HIGH")
    )

    // Schedule Slot Templates according to day of week
    // 0=Sun, 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat
    val WEEKDAY_SLOTS = mapOf(
        // Sunday (5 Slots = 15h)
        0 to listOf("PYQ / Test Analysis", "12 Physics", "12 Maths", "12 Chemistry", "11th Revision / Backlog"),
        // Monday (4 Slots = 12h)
        1 to listOf("12 Physics", "12 Maths", "12 Chemistry", "11 Physics"),
        // Tuesday (4 Slots = 12h)
        2 to listOf("12 Chemistry", "12 Maths", "12 Physics", "11 Maths"),
        // Wednesday (5 Slots = 15h)
        3 to listOf("12 Maths", "12 Physics", "12 Chemistry", "11 Chemistry", "PYQ / Practice Test"),
        // Thursday (4 Slots = 12h)
        4 to listOf("12 Physics", "12 Chemistry", "12 Maths", "11 Physics"),
        // Friday (4 Slots = 12h)
        5 to listOf("12 Chemistry", "12 Maths", "11 Maths", "12 Physics"),
        // Saturday (5 Slots = 15h)
        6 to listOf("12 Maths", "12 Physics", "12 Chemistry", "11 Chemistry", "11 Maths / Test")
    )

    // Daily plan descriptions matching PDF day-by-day table (Days 1 to 60)
    data class DayScheduleInfo(
        val physics: String,
        val maths: String,
        val chem: String,
        val backlogsOrTest: String
    )

    val DAY_SCHEDULES: Map<Int, DayScheduleInfo> = mapOf(
        1 to DayScheduleInfo("Electrostatics", "Determinants", "Solutions", "11th Basic/Kinematics"),
        2 to DayScheduleInfo("Electrostatics", "Matrices", "Solutions", "11th Kinematics"),
        3 to DayScheduleInfo("Electrostatics", "Matrices", "Solutions", "11th NLM"),
        4 to DayScheduleInfo("Electrostatics", "Functions", "Solutions", "11th NLM"),
        5 to DayScheduleInfo("Current Electricity", "Functions", "Kinetics", "11th WEP"),
        6 to DayScheduleInfo("Current Electricity", "Functions", "Kinetics", "11th WEP"),
        7 to DayScheduleInfo("Current Electricity", "Inverse Trig", "Electrochemistry", "11th COM"),
        8 to DayScheduleInfo("Capacitance", "Limits", "Electrochemistry", "11th COM"),
        9 to DayScheduleInfo("Capacitance", "Limits", "Coordination", "11th Rotation"),
        10 to DayScheduleInfo("Capacitance", "Differentiation", "Coordination", "11th Rotation"),
        11 to DayScheduleInfo("Magnetism", "AOD", "p-Block", "11th Thermodynamics"),
        12 to DayScheduleInfo("Magnetism", "AOD", "d/f Block", "11th Thermodynamics"),
        13 to DayScheduleInfo("EMI", "Indefinite Integration", "GOC", "11th Equilibrium"),
        14 to DayScheduleInfo("AC", "Definite Integration", "GOC", "11th Equilibrium"),
        15 to DayScheduleInfo("Ray Optics", "Definite Integration", "Isomerism", "11th Bonding"),
        16 to DayScheduleInfo("Ray Optics", "Area", "Hydrocarbons", "11th Trigonometry"),
        17 to DayScheduleInfo("Ray Optics", "Differential Equations", "Hydrocarbons", "11th Quadratic"),
        18 to DayScheduleInfo("Wave Optics", "Vectors", "Haloalkanes", "11th Sequence & Series"),
        19 to DayScheduleInfo("Wave Optics", "Vectors", "Alcohol/Phenol/Ether", "11th P&C"),
        20 to DayScheduleInfo("Modern Physics", "3-D Geometry", "Carbonyl", "11th Straight Line"),
        21 to DayScheduleInfo("Modern Physics", "3-D Geometry", "Carbonyl", "11th Circle"),
        22 to DayScheduleInfo("Modern Physics", "Probability", "Amines", "11th Conics"),
        23 to DayScheduleInfo("Semiconductor", "Probability", "Biomolecules", "11th Revision"),
        24 to DayScheduleInfo("PYQ Physics", "PYQ Maths", "PYQ Chemistry", "Mixed Mock Test"),
        25 to DayScheduleInfo("Prarambh Module", "Prarambh Module", "Prarambh Module", "Formula Revision"),
        26 to DayScheduleInfo("Prabal Module", "Prabal Module", "Prabal Module", "Error Log Audit"),
        27 to DayScheduleInfo("Prakshit Module", "Prakshit Module", "Prakshit Module", "Part Test"),
        28 to DayScheduleInfo("Module Physics", "Module Maths", "Module Chemistry", "DPP Backlog"),
        29 to DayScheduleInfo("NCERT / IOC", "Revision Physics", "Revision Maths", "11th PYQ"),
        30 to DayScheduleInfo("Weak Chapter", "Weak Chapter", "Weak Chapter", "Mistake Analysis"),
        31 to DayScheduleInfo("Mixed Test", "Test Analysis", "DPP Weak", "NCERT Revision"),
        32 to DayScheduleInfo("PYQ Physics", "PYQ Maths", "PYQ Chemistry", "Mixed Test"),
        33 to DayScheduleInfo("Prarambh Module", "Prarambh Module", "Prarambh Module", "Formula Revision"),
        34 to DayScheduleInfo("Prabal Module", "Prabal Module", "Prabal Module", "Error Log"),
        35 to DayScheduleInfo("Prakshit Module", "Prakshit Module", "Prakshit Module", "Part Test"),
        36 to DayScheduleInfo("Module Physics", "Module Maths", "Module Chemistry", "DPP Backlog"),
        37 to DayScheduleInfo("NCERT / IOC", "Revision Physics", "Revision Maths", "11th PYQ"),
        38 to DayScheduleInfo("Weak Chapter", "Weak Chapter", "Weak Chapter", "Mistake Analysis"),
        39 to DayScheduleInfo("Mixed Test", "Test Analysis", "DPP Weak", "Revision"),
        40 to DayScheduleInfo("PYQ Physics", "PYQ Maths", "PYQ Chemistry", "Mixed Test"),
        41 to DayScheduleInfo("Prarambh Module", "Prarambh Module", "Prarambh Module", "Formula Revision"),
        42 to DayScheduleInfo("Prabal Module", "Prabal Module", "Prabal Module", "Error Log"),
        43 to DayScheduleInfo("Prakshit Module", "Prakshit Module", "Prakshit Module", "Part Test"),
        44 to DayScheduleInfo("Module Physics", "Module Maths", "Module Chemistry", "DPP Backlog"),
        45 to DayScheduleInfo("NCERT / IOC", "Revision Physics", "Revision Maths", "11th PYQ"),
        46 to DayScheduleInfo("Weak Chapter", "Weak Chapter", "Weak Chapter", "Mistake Analysis"),
        47 to DayScheduleInfo("Mixed Test", "Test Analysis", "DPP Weak", "Revision"),
        48 to DayScheduleInfo("PYQ Physics", "PYQ Maths", "PYQ Chemistry", "Mixed Test"),
        49 to DayScheduleInfo("Prarambh Module", "Prarambh Module", "Prarambh Module", "Formula Revision"),
        50 to DayScheduleInfo("Prabal Module", "Prabal Module", "Prabal Module", "Error Log"),
        51 to DayScheduleInfo("Prakshit Module", "Prakshit Module", "Prakshit Module", "Part Test"),
        52 to DayScheduleInfo("Module Physics", "Module Maths", "Module Chemistry", "DPP Backlog"),
        53 to DayScheduleInfo("NCERT / IOC", "Revision Physics", "Revision Maths", "11th PYQ"),
        54 to DayScheduleInfo("Weak Chapter", "Weak Chapter", "Weak Chapter", "Mistake Analysis"),
        55 to DayScheduleInfo("Mixed Test", "Test Analysis", "DPP Weak", "Revision"),
        56 to DayScheduleInfo("Buffer Missed", "Buffer Missed", "PYQ Must-do", "NCERT Chem"),
        57 to DayScheduleInfo("Revision High-W", "Revision High-W", "Revision High-W", "Error Log"),
        58 to DayScheduleInfo("Test", "Analysis", "Weak Practice", "11th Revision"),
        59 to DayScheduleInfo("Final Buffer", "Final Buffer", "Final PYQ", "Formula Sheets"),
        60 to DayScheduleInfo("DAY 60 Final", "DAY 60 Final", "DAY 60 Final", "Carryover List & Grand Review")
    )

    // Connected Chapter Flow from PDF page 12
    val CONNECTED_FLOWS = listOf(
        ConnectedFlowNode(
            subject = "Physics Flow",
            icon = Icons.Default.ElectricBolt,
            color = Color(0xFF6750A4),
            steps = listOf(
                "Units & Dimensions",
                "Kinematics",
                "NLM & Friction",
                "Work, Energy & Power",
                "COM & Momentum",
                "Rotational Dynamics",
                "Electrostatics",
                "Current Electricity",
                "Magnetism",
                "EMI",
                "AC",
                "Optics",
                "Modern Physics"
            )
        ),
        ConnectedFlowNode(
            subject = "Maths Calculus & Algebra Flow",
            icon = Icons.Default.Calculate,
            color = Color(0xFF00639B),
            steps = listOf(
                "Basic Maths & Logs",
                "Quadratic Equations",
                "Trigonometry",
                "Relations & Functions",
                "Limits",
                "Continuity & Diff.",
                "Application of Derivatives (AOD)",
                "Indefinite Integration",
                "Definite Integration & Area",
                "Differential Equations"
            )
        ),
        ConnectedFlowNode(
            subject = "Coordinate & Vectors Flow",
            icon = Icons.Default.CompassCalibration,
            color = Color(0xFF7D5260),
            steps = listOf(
                "Straight Line",
                "Circle",
                "Conics (Parabola, Ellipse, Hyperbola)",
                "Vectors",
                "3-D Geometry"
            )
        ),
        ConnectedFlowNode(
            subject = "Physical Chemistry Flow",
            icon = Icons.Default.Science,
            color = Color(0xFF00875A),
            steps = listOf(
                "Mole Concept",
                "Atomic Structure",
                "Thermodynamics",
                "Chemical & Ionic Equilibrium",
                "Solutions",
                "Electrochemistry",
                "Chemical Kinetics"
            )
        ),
        ConnectedFlowNode(
            subject = "Organic Chemistry Flow",
            icon = Icons.Default.Biotech,
            color = Color(0xFFB26A00),
            steps = listOf(
                "IUPAC Nomenclature",
                "General Organic Chemistry (GOC)",
                "Isomerism (Structural + Optical)",
                "Hydrocarbons",
                "Haloalkanes & Haloarenes",
                "Alcohol, Phenol & Ether",
                "Carbonyl Compounds & Acids",
                "Amines & Diazonium",
                "Biomolecules"
            )
        ),
        ConnectedFlowNode(
            subject = "Inorganic Chemistry Flow",
            icon = Icons.Default.Timeline,
            color = Color(0xFF5266CF),
            steps = listOf(
                "Atomic Structure & Quantum",
                "Periodic Properties & Trends",
                "Chemical Bonding & MOT",
                "p-Block Elements",
                "d & f-Block Elements",
                "Coordination Compounds",
                "Salt Analysis"
            )
        )
    )

    // Faculty Stack
    val FACULTY_STACK = listOf(
        FacultyStack("Physics", "Saleem Ahmad Sir", "Lakshya JEE 2027 / 11th Restart", "113 planned Lakshya lectures + 54 11th Restart lectures"),
        FacultyStack("Mathematics", "Sachin Jakhar Sir", "Lakshya JEE 2027", "90 planned Lakshya lectures; deep rigorous problem-solving"),
        FacultyStack("11th Maths", "Anna Sir", "One-Shot Route", "High-yield one-shots for fast coverage & revision"),
        FacultyStack("Physical Chem", "Faisal Razaq Sir", "Lakshya JEE 2027 / 11th Restart", "24 PC lectures; calculation & conceptual clarity"),
        FacultyStack("Inorganic Chem", "Om Pandey Sir", "Lakshya JEE 2027 / 11th Restart", "Coordination, p-block, d/f block, salt analysis"),
        FacultyStack("Organic Chem", "Pankaj Sijariya Sir", "Lakshya JEE 2027 / 11th Restart", "GOC, mechanisms, stereochemistry, named reactions")
    )

    // Weeks metadata
    val WEEKS = listOf(
        Triple(1, "01 Sep → 07 Sep 2026", "Electrostatics, Current, Determinants/Matrices, Solutions, 11th Kinematics"),
        Triple(2, "08 Sep → 14 Sep 2026", "Capacitance, Magnetism, Functions/Limits, Kinetics/Electrochem, 11th NLM/WEP"),
        Triple(3, "15 Sep → 21 Sep 2026", "EMI/AC, AOD/Integration, Coordination/p/d-f Block, 11th Rotation/Thermo"),
        Triple(4, "22 Sep → 28 Sep 2026", "Optics, Definite Integration/Area, GOC/Isomerism/Hydrocarbons, 11th Equilibrium"),
        Triple(5, "29 Sep → 05 Oct 2026", "Modern Physics, DE/Vectors, Haloalkanes/Alcohols, 11th Coordinate"),
        Triple(6, "06 Oct → 12 Oct 2026", "Semiconductors, 3D/Probability, Carbonyl/Amines, 11th Chem Revision"),
        Triple(7, "13 Oct → 19 Oct 2026", "12th NCERT Backlog Review + Prarambh + Prabal + Prakshit + Heavy PYQs"),
        Triple(8, "20 Oct → 26 Oct 2026", "Mixed PYQs + Module Completion + Mock Tests + Error Analysis"),
        Triple(9, "27 Oct → 30 Oct 2026", "Buffer Days + High-Weightage NCERT Revision + Formula Sheets + Error Log")
    )

    // ==========================================
    // YAKEEN NEET (MEDICAL) CURRICULUM
    // ==========================================
    val NEET_CHAPTERS: List<ChapterItem> = listOf(
        // Class 11 Biology (Botany & Zoology)
        ChapterItem(100, "11 Biology", "Living World & Biological Classification", "6", "9", "5-kingdom system, monera, fungi, viruses, taxonomy", "HIGH"),
        ChapterItem(101, "11 Biology", "Plant Kingdom", "8", "12", "Algae, bryophytes, pteridophytes, gymnosperms, life cycles", "HIGH"),
        ChapterItem(102, "11 Biology", "Animal Kingdom", "9", "14", "Porifera to Chordata, symmetry, coelom, key NCERT examples", "HIGH"),
        ChapterItem(103, "11 Biology", "Morphology & Anatomy of Flowering Plants", "10", "15", "Root, stem, leaf modifications, flowers, tissues, secondary growth", "HIGH"),
        ChapterItem(104, "11 Biology", "Structural Organisation in Animals", "5", "8", "Epithelial, connective, muscular tissues, frog anatomy", "MED"),
        ChapterItem(105, "11 Biology", "Cell: Unit of Life & Biomolecules", "11", "17", "Organelles, membrane, enzymes, amino acids, nucleic acids", "HIGH"),
        ChapterItem(106, "11 Biology", "Cell Cycle and Cell Division", "6", "9", "Mitosis, meiosis stages, crossing over, checkpoints", "HIGH"),
        ChapterItem(107, "11 Biology", "Photosynthesis & Respiration in Plants", "10", "15", "Light reaction, Calvin cycle, C4/CAM, glycolysis, Krebs cycle", "HIGH"),
        ChapterItem(108, "11 Biology", "Plant Growth and Development", "5", "8", "Auxin, gibberellin, cytokinin, ethylene, ABA, plasticity", "MED"),
        ChapterItem(109, "11 Biology", "Breathing & Exchange of Gases", "6", "9", "Respiratory volumes, transport of O2/CO2, neural regulation", "HIGH"),
        ChapterItem(110, "11 Biology", "Body Fluids and Circulation", "7", "11", "Blood groups, cardiac cycle, ECG, double circulation, disorders", "HIGH"),
        ChapterItem(111, "11 Biology", "Excretory Products and Elimination", "6", "9", "Nephron structure, urine formation, counter-current mechanism, RAAS", "HIGH"),
        ChapterItem(112, "11 Biology", "Locomotion and Movement", "5", "8", "Sliding filament theory, sarcomere, joints, skeletal system", "HIGH"),
        ChapterItem(113, "11 Biology", "Neural Control & Chemical Coordination", "9", "14", "Action potential, reflex arc, endocrine glands, hormones feedback", "HIGH"),

        // Class 12 Biology (Botany & Zoology)
        ChapterItem(114, "12 Biology", "Sexual Reproduction in Flowering Plants", "8", "12", "Microsporogenesis, megasporogenesis, double fertilization, apomixis", "HIGH"),
        ChapterItem(115, "12 Biology", "Human Reproduction & Reproductive Health", "10", "15", "Gametogenesis, menstrual cycle, fertilization, contraception, ART", "HIGH"),
        ChapterItem(116, "12 Biology", "Principles of Inheritance & Variation", "13", "20", "Mendelian genetics, sex determination, mutation, pedigree analysis", "HIGH"),
        ChapterItem(117, "12 Biology", "Molecular Basis of Inheritance", "14", "22", "DNA replication, transcription, genetic code, translation, lac operon", "HIGH"),
        ChapterItem(118, "12 Biology", "Evolution", "7", "11", "Homology/analogy, Hardy-Weinberg, Darwinism, human evolution stages", "HIGH"),
        ChapterItem(119, "12 Biology", "Human Health and Disease", "8", "12", "Innate/acquired immunity, AIDS, cancer, common pathogens, drugs", "HIGH"),
        ChapterItem(120, "12 Biology", "Microbes in Human Welfare", "4", "6", "Biocontrol agents, biofertilizers, sewage treatment plants", "MED"),
        ChapterItem(121, "12 Biology", "Biotechnology: Principles & Processes", "7", "11", "Restriction enzymes, cloning vectors, PCR, agarose gel electrophoresis", "HIGH"),
        ChapterItem(122, "12 Biology", "Biotechnology & Its Applications", "6", "9", "Bt cotton, RNAi silencing, gene therapy, transgenic animals", "HIGH"),
        ChapterItem(123, "12 Biology", "Organisms and Populations", "6", "9", "Adaptations, exponential/logistic growth, mutualism, parasitism", "HIGH"),
        ChapterItem(124, "12 Biology", "Ecosystem & Biodiversity Conservation", "8", "12", "Energy flow, pyramids, biodiversity hotspots, in-situ/ex-situ", "HIGH"),

        // NEET Physics (Full NCERT Coverage)
        ChapterItem(125, "Physics", "Units, Dimensions & Errors", "4", "6", "Vernier calipers, screw gauge, dimension analysis, error propagation", "HIGH"),
        ChapterItem(126, "Physics", "Kinematics 1D & 2D", "9", "14", "Graphs, projectile motion, relative velocity in 2D", "HIGH"),
        ChapterItem(127, "Physics", "Laws of Motion & Friction", "8", "12", "FBD, friction coefficient, banking of roads, circular dynamics", "HIGH"),
        ChapterItem(128, "Physics", "Work, Energy and Power", "7", "10", "Work-energy theorem, spring systems, power, collisions", "HIGH"),
        ChapterItem(129, "Physics", "Rotational Dynamics & COM", "10", "15", "Torque, moment of inertia, rolling motion, angular momentum", "HIGH"),
        ChapterItem(130, "Physics", "Gravitation", "5", "8", "Kepler's laws, g variation with height/depth, escape velocity", "HIGH"),
        ChapterItem(131, "Physics", "Mechanical Properties of Solids & Fluids", "7", "10", "Young's modulus, Pascal's law, surface tension, Bernoulli theorem", "MED"),
        ChapterItem(132, "Physics", "Thermal Physics & Thermodynamics", "9", "14", "Calorimetry, 1st law, heat engines, KTG gas laws", "HIGH"),
        ChapterItem(133, "Physics", "Oscillations & Waves (SHM)", "8", "12", "Spring-mass, simple pendulum, Doppler effect, resonance", "HIGH"),
        ChapterItem(134, "Physics", "Electrostatics & Capacitors", "12", "18", "Coulomb's law, dipole, Gauss law flux, parallel plate capacitor", "HIGH"),
        ChapterItem(135, "Physics", "Current Electricity", "10", "15", "Ohm's law, Kirchhoff rules, potentiometer, cells in series/parallel", "HIGH"),
        ChapterItem(136, "Physics", "Magnetic Effects of Current & Magnetism", "9", "14", "Biot-Savart, Ampere's loop, solenoid, Earth magnetism", "HIGH"),
        ChapterItem(137, "Physics", "Electromagnetic Induction & AC", "9", "14", "Faraday-Lenz, self/mutual inductance, LCR resonance, transformer", "HIGH"),
        ChapterItem(138, "Physics", "Ray & Wave Optics", "12", "18", "Prisms, lenses, microscope/telescope, YDSE, polarization", "HIGH"),
        ChapterItem(139, "Physics", "Modern Physics & Semiconductors", "10", "15", "Photoelectric, Bohr model, nuclei mass defect, p-n diode, logic", "HIGH"),

        // NEET Chemistry (Physical, Inorganic & Organic)
        ChapterItem(140, "Chemistry", "Some Basic Concepts (Mole Concept)", "6", "9", "Stoichiometry, limiting reagent, molarity, empirical formula", "HIGH"),
        ChapterItem(141, "Chemistry", "Structure of Atom", "6", "9", "Quantum numbers, Bohr model, de Broglie, orbital shapes", "HIGH"),
        ChapterItem(142, "Chemistry", "Classification of Elements & Periodicity", "5", "8", "IE, EA, electronegativity trends, ionic radii anomalies", "HIGH"),
        ChapterItem(143, "Chemistry", "Chemical Bonding & Molecular Structure", "10", "15", "VSEPR, hybridization, dipole moment, MOT bond orders", "HIGH"),
        ChapterItem(144, "Chemistry", "Chemical Thermodynamics & Equilibrium", "12", "18", "Enthalpy, Hess law, Le Chatelier, pH, buffers, Ksp", "HIGH"),
        ChapterItem(145, "Chemistry", "Redox Reactions & Electrochemistry", "9", "14", "Balancing, Nernst equation, conductance, galvanic cell, Faraday", "HIGH"),
        ChapterItem(146, "Chemistry", "Solutions & Colligative Properties", "7", "11", "Raoult's law, elevation b.p., depression f.p., osmotic pressure", "HIGH"),
        ChapterItem(147, "Chemistry", "Chemical Kinetics", "7", "11", "Rate equation, 1st order integrated law, Arrhenius equation", "HIGH"),
        ChapterItem(148, "Chemistry", "p-Block & d/f-Block Elements", "11", "17", "Oxidation states, lanthanide contraction, catalytic properties", "HIGH"),
        ChapterItem(149, "Chemistry", "Coordination Compounds", "8", "12", "IUPAC, Werner theory, CFT, isomerism, spectrochemical series", "HIGH"),
        ChapterItem(150, "Chemistry", "Organic Chem: GOC & Hydrocarbons", "12", "18", "Inductive, resonance, hyperconjugation, electrophilic addition", "HIGH"),
        ChapterItem(151, "Chemistry", "Haloalkanes, Alcohols & Phenols", "10", "15", "SN1/SN2 mechanisms, Lucas test, Reimer-Tiemann, Kolbe", "HIGH"),
        ChapterItem(152, "Chemistry", "Aldehydes, Ketones & Carboxylic Acids", "10", "15", "Aldol, Cannizzaro, Tollens/Fehling, nucleophilic addition", "HIGH"),
        ChapterItem(153, "Chemistry", "Amines & Biomolecules", "8", "12", "Hinsberg test, diazonium coupling, carbohydrates, proteins, DNA/RNA", "HIGH")
    )

    val NEET_CONNECTED_FLOWS = listOf(
        ConnectedFlowNode(
            subject = "Biology Botany & Ecology Flow",
            icon = Icons.Default.Biotech,
            color = Color(0xFF00875A),
            steps = listOf(
                "Living World",
                "Plant Kingdom",
                "Morphology of Flowering Plants",
                "Anatomy of Plants",
                "Photosynthesis in Plants",
                "Plant Growth & Hormones",
                "Plant Reproduction",
                "Organisms & Populations",
                "Ecosystem & Conservation"
            )
        ),
        ConnectedFlowNode(
            subject = "Cell Biology & Genetics Flow",
            icon = Icons.Default.Science,
            color = Color(0xFF5266CF),
            steps = listOf(
                "Cell: The Unit of Life",
                "Biomolecules",
                "Cell Cycle & Division",
                "Principles of Inheritance",
                "Molecular Basis of Inheritance",
                "Biotechnology Principles",
                "Biotech Applications"
            )
        ),
        ConnectedFlowNode(
            subject = "Zoology & Human Physiology Flow",
            icon = Icons.Default.Timeline,
            color = Color(0xFFD81B60),
            steps = listOf(
                "Animal Kingdom",
                "Structural Organisation (Tissues)",
                "Breathing & Gas Exchange",
                "Circulation & Cardiac Cycle",
                "Excretion & Kidney Osmoregulation",
                "Locomotion & Muscles",
                "Neural & Endocrine Systems",
                "Human Reproduction",
                "Human Health & Immunology"
            )
        ),
        ConnectedFlowNode(
            subject = "NEET Physics Core Flow",
            icon = Icons.Default.ElectricBolt,
            color = Color(0xFF6750A4),
            steps = listOf(
                "Units & Dimensions",
                "Kinematics 1D & 2D",
                "Newton's Laws & Friction",
                "Work-Energy-Power",
                "Rotational Motion",
                "Thermodynamics & KTG",
                "Electrostatics & Capacitors",
                "Current Electricity",
                "Magnetism & AC",
                "Ray & Wave Optics",
                "Modern Physics & Semiconductors"
            )
        ),
        ConnectedFlowNode(
            subject = "NEET Chemistry Mastery Flow",
            icon = Icons.Default.Science,
            color = Color(0xFF00897B),
            steps = listOf(
                "Mole Concept",
                "Atomic Structure & Periodic Table",
                "Chemical Bonding & MOT",
                "Thermodynamics & Equilibrium",
                "Solutions & Kinetics",
                "General Organic Chemistry (GOC)",
                "Hydrocarbons & Alkyl Halides",
                "Carbonyl Compounds & Acids",
                "Coordination Compounds",
                "Biomolecules & NCERT Tables"
            )
        )
    )

    val NEET_FACULTY_STACK = listOf(
        FacultyStack("Botany", "Tarun Kumar / Rupesh Sir", "Yakeen NEET 2026", "NCERT line-by-line botanical mastery, diagrams, and cell biology"),
        FacultyStack("Zoology", "Nomesh Sir / Samapti Ma'am", "Yakeen NEET 2026", "Animal kingdom mnemonics, human physiology and high-yield genetics"),
        FacultyStack("Physics", "Saleem Ahmad / MR Sir", "Yakeen NEET 2026", "Concept visualization, formula shortcuts, and NEET PYQ speed tricks"),
        FacultyStack("Physical Chem", "Sudhanshu Tomar Sir", "Yakeen NEET 2026", "Rapid numerical calculation techniques and formula sheets"),
        FacultyStack("Inorganic Chem", "Om Pandey Sir", "Yakeen NEET 2026", "100% NCERT line-by-line coverage, tables, and exception analysis"),
        FacultyStack("Organic Chem", "Pankaj Sijariya Sir", "Yakeen NEET 2026", "Reaction mechanisms, named reactions, and NCERT conversions")
    )

    val NEET_WEEKS = listOf(
        Triple(1, "01 Sep → 07 Sep 2026", "Living World, Cell, Mole Concept, Units & 1D Kinematics"),
        Triple(2, "08 Sep → 14 Sep 2026", "Plant Kingdom, Biomolecules, Atomic Structure, NLM & Friction"),
        Triple(3, "15 Sep → 21 Sep 2026", "Animal Kingdom, Cell Division, Bonding, Work-Energy-Power"),
        Triple(4, "22 Sep → 28 Sep 2026", "Morphology/Anatomy, Equilibrium, Rotation, Gravitation"),
        Triple(5, "29 Sep → 05 Oct 2026", "Human Physiology Core, Solutions, Kinetics, Electrostatics"),
        Triple(6, "06 Oct → 12 Oct 2026", "Genetics & Inheritance, GOC, Current Electricity, Magnetism"),
        Triple(7, "13 Oct → 19 Oct 2026", "Molecular Basis, Carbonyls, EMI/AC, Optics"),
        Triple(8, "20 Oct → 26 Oct 2026", "Biotech, Ecology, Coordination, Modern Physics"),
        Triple(9, "27 Oct → 30 Oct 2026", "NEET 720 Mock Sprints + NCERT Line-by-Line Revision")
    )
}
