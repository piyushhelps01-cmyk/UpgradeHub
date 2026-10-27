package com.example.data.repository

import android.content.Context
import com.example.data.local.DailyGoalDao
import com.example.data.local.DailyGoalEntity
import com.example.data.local.DailyStudyScheduleDao
import com.example.data.local.DailyStudyScheduleEntity
import com.example.data.model.AutoPlanConfiguration
import com.example.data.model.AutoPlanResult
import com.example.data.model.BatchChapterPlan
import com.example.data.model.BatchCompletionStats
import com.example.data.model.BatchCurriculum
import com.example.data.model.ChapterItem
import com.example.data.model.FacultyStack
import com.example.data.model.SubjectBatchProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

class BatchCurriculumRepository(
    private val context: Context,
    private val dailyGoalDao: DailyGoalDao,
    private val dailyStudyScheduleDao: DailyStudyScheduleDao? = null
) {
    private val prefs = context.getSharedPreferences("jee_batch_prefs", Context.MODE_PRIVATE)

    companion object {
        val EXAM_CATEGORIES = listOf(
            "All Exams",
            "JEE Main & Adv",
            "NEET Medical",
            "Class 8–10",
            "Class 11–12",
            "SSC (CGL/CHSL)",
            "Railway Group D",
            "NDA / Other Exams"
        )

        val PRESET_BATCHES = listOf(
            BatchCurriculum(
                id = "jee_class_12",
                title = "PW Lakshya JEE 2026 (Class 12th Complete)",
                examType = "JEE Main & Advanced",
                classGrade = "Class 12th",
                sourceUrl = "https://pw.live/batches/lakshya-jee-2026-curriculum",
                description = "Complete Class 12th syllabus with lectures, DPPs, and Advanced problem solving across Physics, Chemistry, and Mathematics.",
                totalLectures = 244,
                totalHours = 366f,
                platform = "PW",
                chapters = listOf(
                    // Physics (14 chapters, 88 lectures)
                    BatchChapterPlan("Physics", "Electrostatics & Gauss Law", "Class 12", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Physics", "Current Electricity", "Class 12", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Physics", "Capacitance", "Class 12", 6, 9f, "Medium"),
                    BatchChapterPlan("Physics", "Moving Charges & Magnetism", "Class 12", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Physics", "Magnetism and Matter", "Class 12", 4, 6f, "Fundamental"),
                    BatchChapterPlan("Physics", "Electromagnetic Induction", "Class 12", 7, 10.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Alternating Current", "Class 12", 6, 9f, "Medium"),
                    BatchChapterPlan("Physics", "Electromagnetic Waves", "Class 12", 3, 4.5f, "Fundamental"),
                    BatchChapterPlan("Physics", "Ray Optics & Optical Instruments", "Class 12", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Wave Optics", "Class 12", 7, 10.5f, "Medium"),
                    BatchChapterPlan("Physics", "Dual Nature of Radiation & Matter", "Class 12", 4, 6f, "High Weightage"),
                    BatchChapterPlan("Physics", "Atoms & Nuclei", "Class 12", 6, 9f, "High Weightage"),
                    BatchChapterPlan("Physics", "Semiconductor Electronics", "Class 12", 4, 6f, "High Weightage"),
                    // Chemistry (10 chapters, 72 lectures)
                    BatchChapterPlan("Chemistry", "Solutions & Colligative Properties", "Class 12", 7, 10.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Electrochemistry", "Class 12", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Chemical Kinetics", "Class 12", 7, 10.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "The d- and f-Block Elements", "Class 12", 6, 9f, "Medium"),
                    BatchChapterPlan("Chemistry", "Coordination Compounds", "Class 12", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Haloalkanes and Haloarenes", "Class 12", 8, 12f, "Medium"),
                    BatchChapterPlan("Chemistry", "Alcohols, Phenols and Ethers", "Class 12", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Aldehydes, Ketones & Carboxylic Acids", "Class 12", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Amines (Organic Nitrogen Compounds)", "Class 12", 5, 7.5f, "Medium"),
                    BatchChapterPlan("Chemistry", "Biomolecules", "Class 12", 3, 4.5f, "Fundamental"),
                    // Mathematics (12 chapters, 84 lectures)
                    BatchChapterPlan("Mathematics", "Relations and Functions", "Class 12", 6, 9f, "Fundamental"),
                    BatchChapterPlan("Mathematics", "Inverse Trigonometric Functions", "Class 12", 5, 7.5f, "Medium"),
                    BatchChapterPlan("Mathematics", "Matrices and Determinants", "Class 12", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Continuity & Differentiability", "Class 12", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Application of Derivatives (AOD)", "Class 12", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Indefinite Integration", "Class 12", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Definite Integrals & Properties", "Class 12", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Area Under Curves", "Class 12", 4, 6f, "Medium"),
                    BatchChapterPlan("Mathematics", "Differential Equations", "Class 12", 7, 10.5f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Vector Algebra", "Class 12", 6, 9f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Three Dimensional Geometry (3D)", "Class 12", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Probability & Bayes Theorem", "Class 12", 4, 6f, "High Weightage")
                ),
                faculty = listOf(
                    FacultyStack("Physics", "Saleem Ahmad Sir", "Lakshya JEE 2026", "113 planned Class 12 lectures; visual derivations & HC Verma + Irodov problem solving"),
                    FacultyStack("Mathematics", "Sachin Jakhar Sir", "Lakshya JEE 2026", "90 planned lectures; rigorous calculus, algebra & coordinate geometry"),
                    FacultyStack("Physical Chem", "Faisal Razaq Sir", "Lakshya JEE 2026", "Solutions, Electrochemistry, Kinetics deep numerical drill & NCERT"),
                    FacultyStack("Inorganic Chem", "Om Pandey Sir", "Lakshya JEE 2026", "Coordination, d/f block, metallurgy NCERT line-by-line"),
                    FacultyStack("Organic Chem", "Pankaj Sijariya Sir", "Lakshya JEE 2026", "Haloalkanes to Amines, reaction mechanisms & named reactions")
                )
            ),
            BatchCurriculum(
                id = "jee_class_11",
                title = "PW Arjuna JEE 2026 (Class 11th Foundation)",
                examType = "JEE Main & Advanced",
                classGrade = "Class 11th",
                sourceUrl = "https://pw.live/batches/arjuna-jee-2026-curriculum",
                description = "Master core fundamentals of Physics, Chemistry, and Maths for Class 11th with full lecture counts and problem sets.",
                totalLectures = 276,
                totalHours = 414f,
                platform = "PW",
                chapters = listOf(
                    // Physics
                    BatchChapterPlan("Physics", "Units, Dimensions & Errors", "Class 11", 4, 6f, "Fundamental"),
                    BatchChapterPlan("Physics", "Kinematics 1D & 2D (Vectors)", "Class 11", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Physics", "Laws of Motion (NLM & Friction)", "Class 11", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Work, Energy and Power", "Class 11", 7, 10.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Center of Mass & Collisions", "Class 11", 6, 9f, "High Weightage"),
                    BatchChapterPlan("Physics", "Rotational Dynamics (Rigid Body)", "Class 11", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Physics", "Gravitation", "Class 11", 5, 7.5f, "Medium"),
                    BatchChapterPlan("Physics", "Mechanical Properties of Solids & Fluids", "Class 11", 8, 12f, "Medium"),
                    BatchChapterPlan("Physics", "Thermal Properties & Thermodynamics", "Class 11", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Physics", "Kinetic Theory of Gases (KTG)", "Class 11", 4, 6f, "Medium"),
                    BatchChapterPlan("Physics", "Simple Harmonic Motion (SHM)", "Class 11", 7, 10.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Waves & Sound", "Class 11", 8, 12f, "High Weightage"),
                    // Chemistry
                    BatchChapterPlan("Chemistry", "Some Basic Concepts (Mole Concept)", "Class 11", 7, 10.5f, "Fundamental"),
                    BatchChapterPlan("Chemistry", "Atomic Structure (Quantum Model)", "Class 11", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Periodic Classification of Elements", "Class 11", 6, 9f, "Fundamental"),
                    BatchChapterPlan("Chemistry", "Chemical Bonding & Molecular Structure", "Class 11", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Chemical & Ionic Equilibrium", "Class 11", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Chemical Thermodynamics", "Class 11", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Redox Reactions", "Class 11", 4, 6f, "Fundamental"),
                    BatchChapterPlan("Chemistry", "Organic Chem: GOC & Isomerism", "Class 11", 13, 19.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Hydrocarbons (Alkanes, Alkenes, Alkynes)", "Class 11", 10, 15f, "High Weightage"),
                    // Mathematics
                    BatchChapterPlan("Mathematics", "Sets, Relations and Functions", "Class 11", 6, 9f, "Fundamental"),
                    BatchChapterPlan("Mathematics", "Trigonometric Functions & Equations", "Class 11", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Complex Numbers & Quadratic Equations", "Class 11", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Linear Inequalities", "Class 11", 3, 4.5f, "Fundamental"),
                    BatchChapterPlan("Mathematics", "Permutations and Combinations (P&C)", "Class 11", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Binomial Theorem", "Class 11", 6, 9f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Sequence and Series (AP, GP, HP)", "Class 11", 7, 10.5f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Straight Lines & Pair of Lines", "Class 11", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Conic Sections (Circle, Parabola, Ellipse, Hyperbola)", "Class 11", 14, 21f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Limits and Derivatives", "Class 11", 6, 9f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Statistics", "Class 11", 4, 6f, "Medium")
                ),
                faculty = listOf(
                    FacultyStack("Physics", "Saleem Ahmad Sir & Rajwant Sir", "Arjuna JEE 2026", "Kinematics, NLM, Rotation, WPE, Fluids & Gravitation fundamentals"),
                    FacultyStack("Mathematics", "Sachin Jakhar Sir & TK Sir", "Arjuna JEE 2026", "Conics, Trigonometry, P&C, Complex Numbers foundation"),
                    FacultyStack("Physical Chem", "Faisal Razaq Sir", "Arjuna JEE 2026", "Mole concept, Atomic structure, Thermodynamics, Equilibrium"),
                    FacultyStack("Inorganic Chem", "Om Pandey Sir & Rahul Dudi Sir", "Arjuna JEE 2026", "Periodic table, Chemical bonding, Redox fundamentals"),
                    FacultyStack("Organic Chem", "Pankaj Sijariya Sir", "Arjuna JEE 2026", "GOC, Isomerism, IUPAC, Hydrocarbons mechanism foundation")
                )
            ),
            BatchCurriculum(
                id = "jee_dropper_turbo",
                title = "Unacademy Phoenix / PW Prayas JEE (Dropper Sprint)",
                examType = "JEE Main & Advanced",
                classGrade = "Dropper / 12th Pass",
                sourceUrl = "https://unacademy.com/batch/phoenix-jee-dropper-2026",
                description = "High-octane revision and problem solving for 12th pass students targeting top percentile in JEE.",
                totalLectures = 320,
                totalHours = 480f,
                platform = "Unacademy",
                chapters = listOf(
                    BatchChapterPlan("Physics", "Kinematics & Dynamics Sprint", "Both", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Physics", "Work-Power-Energy & Rotation", "Both", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Physics", "SHM, Waves & Thermodynamics", "Both", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Electrostatics, Gauss & Capacitors", "Both", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Physics", "Current, Magnetism & EMI/AC", "Both", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Physics", "Optics (Ray & Wave Optics)", "Both", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Physics", "Modern Physics & Semiconductors", "Both", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Mole Concept & Atomic Structure", "Both", 7, 10.5f, "Fundamental"),
                    BatchChapterPlan("Chemistry", "Chemical Bonding & Periodic Table", "Both", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Thermodynamics & Equilibrium", "Both", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Electrochemistry & Kinetics", "Both", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Coordination Chemistry & d-Block", "Both", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "GOC & Reaction Mechanisms", "Both", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Functional Groups (Carbonyls, Alcohols, Amines)", "Both", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Calculus (Limits, Continuity, Derivatives)", "Both", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Integral Calculus (Definite & Differential Eq)", "Both", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Vectors and 3D Geometry", "Both", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Algebra (Matrices, Determinants, P&C, Binomial)", "Both", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Coordinate Geometry (Circles & Conics)", "Both", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Probability & Complex Numbers", "Both", 8, 12f, "High Weightage")
                ),
                faculty = listOf(
                    FacultyStack("Physics", "Saleem Ahmad Sir & Rajwant Sir", "Prayas Dropper 2026", "High-speed 11th+12th combined conceptual revision & PYQ sprint"),
                    FacultyStack("Mathematics", "Ashish Agarwal Sir & Sachin Sir", "Prayas Dropper 2026", "High-octane algebra, calculus and 3D geometry problem marathons"),
                    FacultyStack("Physical Chem", "Sarvesh Dixit Sir & Faisal Sir", "Prayas Dropper 2026", "Shortcuts, formula sheets and high-speed calculation tricks"),
                    FacultyStack("Inorganic Chem", "Amit Mahajan Sir & Om Sir", "Prayas Dropper 2026", "Direct NCERT line extract & exception mastery"),
                    FacultyStack("Organic Chem", "Pankaj Sijariya Sir", "Prayas Dropper 2026", "Reaction charts, conversion roadmaps & multi-step synthesis")
                )
            ),
            BatchCurriculum(
                id = "neet_yakeen",
                title = "PW Yakeen / Unacademy Conquer NEET 2026",
                examType = "NEET (Medical)",
                classGrade = "Class 11th & 12th",
                sourceUrl = "https://pw.live/batches/yakeen-neet-2026",
                description = "Focused NEET syllabus covering Physics, Chemistry, and Biology (Botany & Zoology) with NCERT-strict line-by-line mastery.",
                totalLectures = 310,
                totalHours = 465f,
                platform = "PW",
                chapters = listOf(
                    // Biology (Botany & Zoology - 18 chapters, 140 lectures)
                    BatchChapterPlan("Biology", "Living World & Biological Classification", "Class 11", 6, 9f, "High Weightage"),
                    BatchChapterPlan("Biology", "Plant Kingdom", "Class 11", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Biology", "Animal Kingdom", "Class 11", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Biology", "Morphology & Anatomy of Flowering Plants", "Class 11", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Biology", "Structural Organisation in Animals", "Class 11", 5, 7.5f, "Medium"),
                    BatchChapterPlan("Biology", "Cell: Unit of Life & Biomolecules", "Class 11", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Biology", "Cell Cycle and Cell Division", "Class 11", 6, 9f, "High Weightage"),
                    BatchChapterPlan("Biology", "Plant Physiology (Photosynthesis & Respiration)", "Class 11", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Biology", "Human Physiology Core (Digestion to Circulation)", "Class 11", 14, 21f, "High Weightage"),
                    BatchChapterPlan("Biology", "Excretion, Locomotion & Neural Coordination", "Class 11", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Biology", "Reproduction in Flowering Plants", "Class 12", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Biology", "Human Reproduction & Reproductive Health", "Class 12", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Biology", "Principles of Inheritance & Variation", "Class 12", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Biology", "Molecular Basis of Inheritance", "Class 12", 13, 19.5f, "High Weightage"),
                    BatchChapterPlan("Biology", "Evolution & Human Health and Disease", "Class 12", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Biology", "Biotechnology: Principles & Applications", "Class 12", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Biology", "Organisms & Populations", "Class 12", 6, 9f, "High Weightage"),
                    BatchChapterPlan("Biology", "Ecosystem & Biodiversity Conservation", "Class 12", 7, 10.5f, "High Weightage"),
                    // Physics
                    BatchChapterPlan("Physics", "Units, Dimensions & 1D/2D Kinematics", "Class 11", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Laws of Motion & Friction", "Class 11", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Physics", "Work, Energy, Power & Rotational Motion", "Class 11", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Gravitation & Properties of Bulk Matter", "Class 11", 7, 10.5f, "Medium"),
                    BatchChapterPlan("Physics", "Thermodynamics, KTG & Oscillations", "Class 11", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Electrostatics, Potential & Capacitance", "Class 12", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Current Electricity & Magnetism", "Class 12", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Physics", "EMI, Alternating Current & EM Waves", "Class 12", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Physics", "Ray & Wave Optics (NEET)", "Class 12", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Modern Physics & Semiconductors", "Class 12", 10, 15f, "High Weightage"),
                    // Chemistry
                    BatchChapterPlan("Chemistry", "Mole Concept & Atomic Structure", "Class 11", 8, 12f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Periodic Classification & Chemical Bonding", "Class 11", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Thermodynamics & Equilibrium", "Class 11", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Solutions & Electrochemistry", "Class 12", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Chemical Kinetics & Surface Chem", "Class 12", 7, 10.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Coordination Compounds & d/f Block", "Class 12", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "p-Block Elements (NCERT Focus)", "Both", 7, 10.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "General Organic Chemistry (GOC) & Hydrocarbons", "Class 11", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Haloalkanes, Alcohols, Phenols & Ethers", "Class 12", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Aldehydes, Ketones, Amines & Biomolecules", "Class 12", 10, 15f, "High Weightage")
                ),
                faculty = listOf(
                    FacultyStack("Botany", "Tarun Kumar Sir & Rupesh Sir", "Yakeen NEET 2026", "NCERT botanical line-by-line, plant physiology & genetics"),
                    FacultyStack("Zoology", "Nomesh Sir & Dr. Vipin Sharma", "Yakeen NEET 2026", "Human physiology, animal kingdom, biotechnology & reproduction"),
                    FacultyStack("Physics", "MR Sir & Saleem Ahmad Sir", "Yakeen NEET 2026", "Concept visualization, formula shortcuts & 35-sec NEET PYQ techniques"),
                    FacultyStack("Physical Chem", "Sudhanshu Tomar Sir", "Yakeen NEET 2026", "Rapid calculation, equilibrium & thermodynamics shortcuts"),
                    FacultyStack("Inorganic Chem", "Om Pandey Sir", "Yakeen NEET 2026", "100% NCERT line-by-line, periodic table & coordination"),
                    FacultyStack("Organic Chem", "Pankaj Sijariya Sir", "Yakeen NEET 2026", "Named reactions, NCERT bio-organic & mechanism clarity")
                )
            ),
            BatchCurriculum(
                id = "class_8_10_foundation",
                title = "PW Udaan & Neev (Class 8–10 NCERT & Olympiad Foundation)",
                examType = "Class 8–10 Board & NTSE/Olympiad",
                classGrade = "Class 8th–10th",
                sourceUrl = "https://pw.live/batches/udaan-class-10-foundation",
                description = "Complete NCERT Science, Mathematics, Social Studies & Mental Ability (MAT) foundation for Class 8, 9 & 10 students.",
                totalLectures = 160,
                totalHours = 240f,
                platform = "PW",
                chapters = listOf(
                    BatchChapterPlan("Physics", "Light: Reflection & Refraction (NCERT)", "Class 10", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Physics", "Electricity & Magnetic Effects of Current", "Class 10", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Physics", "Motion, Force, Laws of Motion & Gravitation", "Class 9", 11, 16.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "Force, Pressure, Sound & Light Basics", "Class 8", 8, 12f, "Fundamental"),
                    BatchChapterPlan("Chemistry", "Chemical Reactions, Acids, Bases & Salts", "Class 10", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Metals, Non-Metals & Carbon Compounds", "Class 10", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Atoms, Molecules & Structure of Atom", "Class 9", 9, 13.5f, "High Weightage"),
                    BatchChapterPlan("Biology", "Life Processes, Control & Coordination", "Class 10", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Biology", "Heredity, Reproduction & Cell Fundamental Unit", "Class 9", 10, 15f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Real Numbers, Polynomials & Quadratic Equations", "Class 10", 14, 21f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Triangles, Circles & Trigonometry", "Class 10", 15, 22.5f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Arithmetic Progressions, Coordinate Geometry & Stats", "Class 10", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Number Systems, Lines, Angles & Quadrilaterals", "Class 9", 12, 18f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Linear Equations, Mensuration & Algebraic Identities", "Class 8", 13, 19.5f, "Fundamental")
                ),
                faculty = listOf(
                    FacultyStack("Physics", "Alakh Pandey Sir & Rakshak Sir", "Udaan Class 9-10", "Light, Electricity, Motion, Gravitation & NTSE/Olympiad foundation"),
                    FacultyStack("Mathematics", "Ritik Sir & Mohit Sir", "Udaan Class 9-10", "Real numbers, Geometry, Trigonometry & Olympiad problem solving"),
                    FacultyStack("Chemistry", "Ashish Sir & Sunil Sir", "Udaan Class 9-10", "Chemical reactions, Metals, Carbon compounds & atomic structure"),
                    FacultyStack("Biology", "Samridhi Ma'am", "Udaan Class 9-10", "Life processes, Heredity, Cell structure & diagram mastery"),
                    FacultyStack("Social Science & MAT", "Sanyam Sir", "Udaan Class 9-10", "Mental ability test (MAT), NTSE reasoning & board exam prep")
                )
            ),
            BatchCurriculum(
                id = "ssc_cgl_chsl_kgs",
                title = "KGS (Khan Global Studies) & Unacademy SSC CGL / CHSL Tier 1 & 2",
                examType = "SSC CGL / CHSL / MTS",
                classGrade = "SSC Competitive",
                sourceUrl = "https://khanglobalstudies.com/courses/ssc-cgl-chsl-foundation-2026",
                description = "Complete Quantitative Aptitude, General Intelligence & Reasoning, English Comprehension, and GK/GS by Khan Sir & KGS Faculty.",
                totalLectures = 210,
                totalHours = 315f,
                platform = "KGS",
                chapters = listOf(
                    BatchChapterPlan("Mathematics", "Percentage, Profit & Loss, Discount & SI/CI", "SSC", 18, 27f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Ratio, Time & Work, Pipes & Time-Speed-Distance", "SSC", 16, 24f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Advance Maths: Geometry, Mensuration 2D/3D & Trig", "SSC", 24, 36f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Algebra, Number System & Data Interpretation", "SSC", 16, 24f, "High Weightage"),
                    BatchChapterPlan("Reasoning", "Analogy, Number Series, Coding-Decoding & Syllogism", "SSC", 15, 22.5f, "High Weightage"),
                    BatchChapterPlan("Reasoning", "Blood Relations, Seating Arrangement, Dice & Non-Verbal", "SSC", 15, 22.5f, "Medium"),
                    BatchChapterPlan("English", "Grammar Rules, Error Spotting & Sentence Improvement", "SSC", 18, 27f, "High Weightage"),
                    BatchChapterPlan("English", "Vocab: Synonyms, Antonyms, Idioms, OWS & Cloze Test", "SSC", 18, 27f, "High Weightage"),
                    BatchChapterPlan("General Studies", "Indian Polity, Constitution & History (KGS Special)", "SSC", 25, 37.5f, "High Weightage"),
                    BatchChapterPlan("General Studies", "Geography, Economy & Static GK / Current Affairs", "SSC", 25, 37.5f, "High Weightage"),
                    BatchChapterPlan("Physics", "General Science: Physics, Chemistry & Biology NCERT", "SSC", 20, 30f, "High Weightage")
                ),
                faculty = listOf(
                    FacultyStack("General Studies", "Khan Sir", "KGS SSC Foundation", "Constitution, Indian Polity, Modern History & National Affairs"),
                    FacultyStack("Quantitative Aptitude", "Dharmendra Sir & Rakesh Yadav Sir", "KGS SSC Foundation", "Advance maths, arithmetic tricks, percentage, time-work"),
                    FacultyStack("Reasoning", "PK Sir & Piyush Sir", "KGS SSC Foundation", "Syllogism, Coding-Decoding, Non-verbal, Puzzles & seating"),
                    FacultyStack("English", "Gopal Verma Sir", "KGS SSC Foundation", "Grammar rules, Vocab root words, Cloze test & Reading comprehension"),
                    FacultyStack("General Science", "SK Jha Sir", "KGS SSC Foundation", "NCERT Science, Physics, Chemistry, Biology & Static GK")
                )
            ),
            BatchCurriculum(
                id = "railway_group_d_ntpc",
                title = "KGS Railway Group D, ALP & RRB NTPC Target Batch",
                examType = "Railway Group D / RRB NTPC",
                classGrade = "Railway & Group D",
                sourceUrl = "https://khanglobalstudies.com/courses/rrb-group-d-ntpc-target",
                description = "Focused NCERT General Science (Class 9–10 Numericals), Railway Mathematics, Reasoning & Static GK/Current Affairs.",
                totalLectures = 175,
                totalHours = 262.5f,
                platform = "KGS",
                chapters = listOf(
                    BatchChapterPlan("Physics", "Railway Science: Work, Power, Heat, Electricity & Light Numericals", "Group D", 22, 33f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "NCERT Class 9-10 Periodic Table, Acids-Bases & Chemical Reactions", "Group D", 18, 27f, "High Weightage"),
                    BatchChapterPlan("Biology", "Human Body Systems, Plant Kingdom, Diseases & Vitamins", "Group D", 18, 27f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "BODMAS, Fractions, LCM/HCF, Percentage & Ratio", "Group D", 20, 30f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Time & Work, Train Problems, SI/CI & Mensuration", "Group D", 22, 33f, "High Weightage"),
                    BatchChapterPlan("Reasoning", "Statement & Conclusion, Venn Diagram, Puzzle & Direction", "Group D", 20, 30f, "High Weightage"),
                    BatchChapterPlan("Reasoning", "Clock, Calendar, Coding-Decoding & Series", "Group D", 15, 22.5f, "Medium"),
                    BatchChapterPlan("General Studies", "Current Affairs, Sports, Awards, Railway GK & Polity", "Group D", 40, 60f, "High Weightage")
                ),
                faculty = listOf(
                    FacultyStack("Railway GK", "Khan Sir", "KGS Railway Target", "Current Affairs, Railway History, Indian Geography & Static GK"),
                    FacultyStack("Science Numericals", "SK Jha Sir", "KGS Railway Target", "Class 9-10 Physics numericals, Heat, Electricity, Chemistry reactions"),
                    FacultyStack("Mathematics", "Dharmendra Sir", "KGS Railway Target", "BODMAS, Train speed problems, Fractions, Ratio & Mensuration"),
                    FacultyStack("Reasoning", "Deepak Sir", "KGS Railway Target", "Statement-conclusion, Venn diagrams, Clock-calendar & Direction sense"),
                    FacultyStack("Static GK", "Praveen Sir", "KGS Railway Target", "Sports, Awards, Inventions, Biosphere reserves & Constitution")
                )
            ),
            BatchCurriculum(
                id = "nda_cuet_other_exams",
                title = "Unacademy & PW NDA / CUET / State CET All-in-One Batch",
                examType = "NDA / CUET / Other Exams",
                classGrade = "Class 12 & Competitive",
                sourceUrl = "https://unacademy.com/batch/nda-cuet-complete-2026",
                description = "Comprehensive preparation for NDA Maths & GAT, CUET Domain Subjects, and State Engineering/Medical CETs.",
                totalLectures = 190,
                totalHours = 285f,
                platform = "Unacademy",
                chapters = listOf(
                    BatchChapterPlan("Mathematics", "NDA/CUET Calculus, Matrices, Determinants & Trig", "Class 12", 28, 42f, "High Weightage"),
                    BatchChapterPlan("Mathematics", "Vector, 3D, Probability, Statistics & Complex Numbers", "Class 12", 24, 36f, "High Weightage"),
                    BatchChapterPlan("Physics", "Mechanics, Optics, Electrostatics & Current Electricity", "Class 12", 26, 39f, "High Weightage"),
                    BatchChapterPlan("Chemistry", "Physical, Organic & Inorganic NCERT Core", "Class 12", 24, 36f, "High Weightage"),
                    BatchChapterPlan("English", "Reading Comprehension, Vocabulary & Grammar Drills", "Competitive", 22, 33f, "High Weightage"),
                    BatchChapterPlan("General Studies", "General Ability Test (GAT): History, Geography, Polity & Current Affairs", "Competitive", 66, 99f, "High Weightage")
                ),
                faculty = listOf(
                    FacultyStack("NDA Mathematics", "Arpit Choudhary Sir", "NDA Warriors 2026", "Calculus, 3D, Vectors, Matrices & NDA Paper 1 speed tricks"),
                    FacultyStack("General English", "Dheeraj Sharma Sir", "NDA / CUET Foundation", "Vocabulary, spotting errors, idioms & sentence completion"),
                    FacultyStack("Science & Physics", "Radhika Sharma Ma'am", "NDA GAT Science", "Class 11-12 Physics concepts, Optics, Electricity & NCERT Chemistry"),
                    FacultyStack("General Knowledge", "Sunny Verma Sir", "NDA GAT Current Affairs", "Indian Geography, Modern History, Polity & Current Events")
                )
            )
        )
    }

    fun getActiveBatchId(): String {
        return prefs.getString("active_batch_id", "jee_class_12") ?: "jee_class_12"
    }

    fun setActiveBatchId(id: String) {
        prefs.edit().putString("active_batch_id", id).apply()
    }

    /**
     * Support 2–3 Parallel Batches running simultaneously!
     */
    fun getParallelBatchIds(): List<String> {
        val raw = prefs.getString("parallel_batch_ids", null)
        if (raw.isNullOrBlank()) {
            return listOf(getActiveBatchId(), "jee_class_11")
        }
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(3)
            .ifEmpty { listOf(getActiveBatchId()) }
    }

    fun setParallelBatchIds(ids: List<String>) {
        val clean = ids.distinct().take(3).ifEmpty { listOf(getActiveBatchId()) }
        prefs.edit().putString("parallel_batch_ids", clean.joinToString(",")).apply()
    }

    /**
     * Get batch by ID or match by URL
     */
    fun getBatchById(id: String): BatchCurriculum {
        return PRESET_BATCHES.find { it.id == id } ?: PRESET_BATCHES.first()
    }

    /**
     * Parse or extract curriculum from custom batch URL (PW, Unacademy, KGS, Allen, YouTube, etc.)
     */
    fun parseBatchFromUrl(url: String, customTitle: String = ""): BatchCurriculum {
        val cleanUrl = url.trim().lowercase(Locale.ENGLISH)

        val detectedPlatform = when {
            cleanUrl.contains("unacademy") -> "Unacademy"
            cleanUrl.contains("kgs") || cleanUrl.contains("khanglobal") || cleanUrl.contains("khan") -> "KGS"
            cleanUrl.contains("pw.live") || cleanUrl.contains("physicswallah") || cleanUrl.contains("lakshya") || cleanUrl.contains("arjuna") || cleanUrl.contains("prayas") || cleanUrl.contains("yakeen") || cleanUrl.contains("udaan") -> "PW"
            cleanUrl.contains("allen") -> "Allen"
            cleanUrl.contains("adda") || cleanUrl.contains("careerwill") -> "Adda247"
            cleanUrl.contains("youtube") || cleanUrl.contains("youtu.be") -> "YouTube"
            else -> "PW / Custom Sync"
        }

        val matchedPreset = when {
            cleanUrl.contains("group-d") || cleanUrl.contains("group_d") || cleanUrl.contains("group d") || cleanUrl.contains("railway") || cleanUrl.contains("rrb") || cleanUrl.contains("ntpc") ->
                PRESET_BATCHES.find { it.id == "railway_group_d_ntpc" }
            cleanUrl.contains("ssc") || cleanUrl.contains("cgl") || cleanUrl.contains("chsl") || cleanUrl.contains("mts") || cleanUrl.contains("kgs") || cleanUrl.contains("khanglobal") ->
                PRESET_BATCHES.find { it.id == "ssc_cgl_chsl_kgs" }
            cleanUrl.contains("8") || cleanUrl.contains("9") || cleanUrl.contains("10") || cleanUrl.contains("udaan") || cleanUrl.contains("neev") || cleanUrl.contains("foundation") ->
                PRESET_BATCHES.find { it.id == "class_8_10_foundation" }
            cleanUrl.contains("nda") || cleanUrl.contains("cuet") || cleanUrl.contains("cet") ->
                PRESET_BATCHES.find { it.id == "nda_cuet_other_exams" }
            cleanUrl.contains("dropper") || cleanUrl.contains("prayas") || cleanUrl.contains("repeater") || cleanUrl.contains("phoenix") ->
                PRESET_BATCHES.find { it.id == "jee_dropper_turbo" }
            cleanUrl.contains("11") || cleanUrl.contains("arjuna") || cleanUrl.contains("class-11") ->
                PRESET_BATCHES.find { it.id == "jee_class_11" }
            cleanUrl.contains("neet") || cleanUrl.contains("yakeen") || cleanUrl.contains("biology") || cleanUrl.contains("medical") ->
                PRESET_BATCHES.find { it.id == "neet_yakeen" }
            else -> PRESET_BATCHES.find { it.id == "jee_class_12" }
        } ?: PRESET_BATCHES.first()

        val slugTitle = url.substringAfterLast("/")
            .substringBefore("?")
            .replace("-", " ")
            .replace("_", " ")
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

        val resolvedTitle = when {
            customTitle.isNotBlank() -> customTitle
            slugTitle.length in 4..45 && !slugTitle.startsWith("Http", ignoreCase = true) ->
                "[$detectedPlatform] $slugTitle"
            else -> "[$detectedPlatform Sync] ${matchedPreset.title}"
        }

        return matchedPreset.copy(
            id = "custom_${System.currentTimeMillis()}",
            title = resolvedTitle,
            sourceUrl = url,
            platform = detectedPlatform
        )
    }

    /**
     * Engine to generate and settle complete daily targets, working hours, and chapters across 30–150 days
     * and up to 2–3 parallel batches simultaneously!
     */
    suspend fun generateAndSettleAutoPlan(
        curriculum: BatchCurriculum,
        config: AutoPlanConfiguration,
        parallelBatches: List<BatchCurriculum> = listOf(curriculum)
    ): AutoPlanResult = withContext(Dispatchers.IO) {
        val targetDays = config.targetDays.coerceIn(30, 150)
        val dailyHours = config.dailyWorkingHours.coerceIn(4f, 16f)

        val activeParallelList = parallelBatches.ifEmpty { listOf(curriculum) }.distinctBy { it.id }.take(3)
        val parallelTitles = activeParallelList.map { it.title }

        // Save preferences so the rest of the app immediately adopts new target hours, 30-150 days, and parallel batches
        prefs.edit()
            .putInt("active_batch_days", targetDays)
            .putFloat("active_batch_daily_hours", dailyHours)
            .putString("active_batch_id", curriculum.id)
            .putString("active_batch_title", curriculum.title)
            .putString("parallel_batch_ids", activeParallelList.joinToString(",") { it.id })
            .apply()

        // Interleave chapters across all 2-3 parallel batches so daily schedule progresses on all selected batches in parallel
        val combinedChapters = mutableListOf<Pair<String, BatchChapterPlan>>()
        val maxChapSize = activeParallelList.maxOfOrNull { it.chapters.size } ?: 0
        for (i in 0 until maxChapSize) {
            for (b in activeParallelList) {
                if (i < b.chapters.size) {
                    combinedChapters.add(b.platform to b.chapters[i])
                }
            }
        }

        val allChapters = curriculum.chapters
        val totalLectures = activeParallelList.sumOf { it.totalLectures }.coerceAtLeast(curriculum.totalLectures)
        val lecturesPerDay = totalLectures.toFloat() / targetDays

        val slotHours = (dailyHours / 4f)
        val generatedGoals = mutableListOf<DailyGoalEntity>()

        data class LectureBlock(
            val batchPlatform: String,
            val subject: String,
            val chapterName: String,
            val lectureNum: Int,
            val totalInChapter: Int,
            val priority: String
        )

        val lectureQueue = mutableListOf<LectureBlock>()
        for ((platform, chap) in combinedChapters) {
            for (lec in 1..chap.totalLectures) {
                lectureQueue.add(
                    LectureBlock(
                        batchPlatform = platform,
                        subject = chap.subject,
                        chapterName = chap.chapterName,
                        lectureNum = lec,
                        totalInChapter = chap.totalLectures,
                        priority = chap.priority
                    )
                )
            }
        }

        var queuePointer = 0
        val totalBlocks = lectureQueue.size

        val isNeet = curriculum.examType.contains("NEET", ignoreCase = true) ||
                curriculum.id.contains("neet", ignoreCase = true) ||
                curriculum.id.contains("yakeen", ignoreCase = true)

        for (day in 1..targetDays) {
            val lecturesToday = when {
                day == targetDays -> maxOf(1, totalBlocks - queuePointer)
                else -> maxOf(1, ceil((totalBlocks.toFloat() - queuePointer) / (targetDays - day + 1)).toInt())
            }

            val assignedLectures = mutableListOf<LectureBlock>()
            for (k in 0 until lecturesToday) {
                if (queuePointer < totalBlocks) {
                    assignedLectures.add(lectureQueue[queuePointer])
                    queuePointer++
                }
            }

            val primarySubFallback = if (isNeet) {
                if (day % 3 == 1) "Biology" else if (day % 3 == 2) "Physics" else "Chemistry"
            } else {
                if (day % 3 == 1) "Physics" else if (day % 3 == 2) "Chemistry" else "Mathematics"
            }
            val primarySub = assignedLectures.firstOrNull()?.subject ?: primarySubFallback
            val primaryChapter = assignedLectures.firstOrNull()?.chapterName ?: "Core Syllabus Milestone"
            val primaryPlatform = assignedLectures.firstOrNull()?.batchPlatform ?: curriculum.platform
            val startLec = assignedLectures.firstOrNull()?.lectureNum ?: 1
            val endLec = assignedLectures.lastOrNull()?.lectureNum ?: startLec

            // Slot 0: Primary Batch Lecture Watch Slot
            val lecTitle = if (assignedLectures.isNotEmpty()) {
                "[$primaryPlatform] $primarySub: $primaryChapter (Lec $startLec–$endLec)"
            } else {
                "$primarySub: Chapter Concept Deep Dive & Problem Walkthrough"
            }
            generatedGoals.add(
                DailyGoalEntity(
                    dayNumber = day,
                    title = lecTitle,
                    category = primarySub,
                    targetHours = slotHours,
                    isCompleted = false,
                    priority = "HIGH",
                    slotIndex = 0,
                    notes = if (activeParallelList.size > 1) {
                        "Parallel Batch Sync (${activeParallelList.size} Batches Active): Watch lectures with running notes."
                    } else {
                        "Batch Sync: Watch lectures at 1.25x–1.5x with active running notebook."
                    }
                )
            )

            // Slot 1: Parallel Batch 2 / DPP & Class Homework Practice
            val parallelSecond = activeParallelList.getOrNull(1)
            val dppTitle = if (parallelSecond != null) {
                val pChap = parallelSecond.chapters.getOrNull((day - 1) % parallelSecond.chapters.size.coerceAtLeast(1))
                "[Parallel #2 • ${parallelSecond.platform}] ${pChap?.subject ?: primarySub}: ${pChap?.chapterName ?: "DPP & Practice"}"
            } else {
                "$primarySub: DPP & Class Exercise (25–30 Questions)"
            }
            generatedGoals.add(
                DailyGoalEntity(
                    dayNumber = day,
                    title = dppTitle,
                    category = primarySub,
                    targetHours = slotHours,
                    isCompleted = false,
                    priority = "HIGH",
                    slotIndex = 1,
                    notes = if (isNeet) {
                        "NEET Target: Solve 30-40 NCERT-focused MCQs; highlight doubtful concepts."
                    } else {
                        "Solve timed DPP set; circle tough questions for weekend doubt clearance."
                    }
                )
            )

            // Slot 2: Parallel Batch 3 or Secondary Subject Problem Solving / PYQ
            val parallelThird = activeParallelList.getOrNull(2)
            val secondarySub = if (isNeet) {
                when (primarySub) {
                    "Biology" -> if (day % 2 == 0) "Chemistry" else "Physics"
                    "Physics" -> if (day % 2 == 0) "Biology" else "Chemistry"
                    "Chemistry" -> if (day % 2 == 0) "Biology" else "Physics"
                    else -> if (day % 2 == 0) "Physics" else "Chemistry"
                }
            } else {
                when (primarySub) {
                    "Physics" -> if (day % 2 == 0) "Mathematics" else "Chemistry"
                    "Chemistry" -> if (day % 2 == 0) "Physics" else "Mathematics"
                    else -> if (day % 2 == 0) "Physics" else "Chemistry"
                }
            }
            val secondaryTitle = if (parallelThird != null) {
                val p3Chap = parallelThird.chapters.getOrNull((day - 1) % parallelThird.chapters.size.coerceAtLeast(1))
                "[Parallel #3 • ${parallelThird.platform}] ${p3Chap?.subject ?: secondarySub}: ${p3Chap?.chapterName ?: "PYQ Drill"}"
            } else {
                "$secondarySub: Module & PYQ Practice (30 Questions)"
            }
            generatedGoals.add(
                DailyGoalEntity(
                    dayNumber = day,
                    title = secondaryTitle,
                    category = secondarySub,
                    targetHours = slotHours,
                    isCompleted = false,
                    priority = "NORMAL",
                    slotIndex = 2,
                    notes = "NCERT → Syllabus → Planner Sync: Solve PYQs with stopwatch timer."
                )
            )

            // Slot 3: NCERT → Syllabus → Planner Revision & Mock Slot
            val revCategory = if (day % 4 == 0) "Mock Test" else "Revision"
            val revTitle = if (day % 4 == 0) {
                if (isNeet) "NEET Timed Mock Test / Part Review (720 Pattern)" else "Timed CBT Mock Test / Part Syllabus Review"
            } else {
                "NCERT → Syllabus → Planner: Line-by-Line & Formula Revision"
            }
            generatedGoals.add(
                DailyGoalEntity(
                    dayNumber = day,
                    title = revTitle,
                    category = revCategory,
                    targetHours = slotHours,
                    isCompleted = false,
                    priority = "NORMAL",
                    slotIndex = 3,
                    notes = "Complete NCERT checklist step → Sync with Syllabus Tracker → Verify Daily Planner slot."
                )
            )
        }

        // Clear existing daily goals and replace with fresh auto-planned batch schedule
        dailyGoalDao.clearAllGoals()
        dailyGoalDao.insertGoals(generatedGoals)

        // Settle daily_study_schedules table for complete sync with tracker and planner
        if (dailyStudyScheduleDao != null) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2026)
                set(Calendar.MONTH, Calendar.SEPTEMBER)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.US)

            val generatedSchedules = mutableListOf<DailyStudyScheduleEntity>()
            for (day in 1..targetDays) {
                val dateStr = dateFormat.format(calendar.time)
                val dayOfWeekStr = dayOfWeekFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_MONTH, 1)

                val weekNum = ((day - 1) / 7) + 1
                val goalsForDay = generatedGoals.filter { it.dayNumber == day }
                val g0 = goalsForDay.find { it.slotIndex == 0 }
                val g1 = goalsForDay.find { it.slotIndex == 1 }
                val g2 = goalsForDay.find { it.slotIndex == 2 }
                val g3 = goalsForDay.find { it.slotIndex == 3 }

                val milestone = "Week $weekNum (${activeParallelList.size} Parallel Batch${if (activeParallelList.size > 1) "es" else ""} • $targetDays-Day Plan)"

                generatedSchedules.add(
                    DailyStudyScheduleEntity(
                        dayNumber = day,
                        plannerDurationDays = targetDays,
                        dateText = dateStr,
                        dayOfWeek = dayOfWeekStr,
                        slot1Title = g0?.title ?: "Slot 1: Core Lectures",
                        slot1Target = g0?.notes ?: "Watch lectures attentively with running notes",
                        slot1Done = false,
                        slot1Hours = slotHours,
                        slot2Title = g1?.title ?: "Slot 2: Parallel Batch / DPP",
                        slot2Target = g1?.notes ?: "Solve 25-30 exercise questions",
                        slot2Done = false,
                        slot2Hours = slotHours,
                        slot3Title = g2?.title ?: "Slot 3: Module & PYQs",
                        slot3Target = g2?.notes ?: "Timed PYQ problem set",
                        slot3Done = false,
                        slot3Hours = slotHours,
                        slot4Title = g3?.title ?: "Slot 4: NCERT → Syllabus Revision",
                        slot4Target = g3?.notes ?: "NCERT line-by-line & mistake notebook",
                        slot4Done = false,
                        slot4Hours = slotHours,
                        actualHours = 0f,
                        targetHours = dailyHours,
                        dayScore = 0,
                        weakTopics = "",
                        nextDayPlan = "Execute next day's 4 focus slots strictly",
                        isCompleted = false,
                        curriculumMilestone = milestone
                    )
                )
            }
            dailyStudyScheduleDao.clearSchedulesForDuration(targetDays)
            dailyStudyScheduleDao.insertSchedules(generatedSchedules)
        }

        AutoPlanResult(
            batchTitle = curriculum.title,
            examType = curriculum.examType,
            classGrade = curriculum.classGrade,
            totalLectures = totalLectures,
            totalChapters = allChapters.size,
            targetDays = targetDays,
            dailyWorkingHours = dailyHours,
            lecturesPerDay = lecturesPerDay,
            totalGeneratedGoals = generatedGoals.size,
            previewGoalsForDay1 = generatedGoals.filter { it.dayNumber == 1 },
            parallelBatchesCount = activeParallelList.size,
            parallelBatchTitles = parallelTitles
        )
    }

    fun setActiveTargetDays(days: Int) {
        prefs.edit().putInt("active_batch_days", days.coerceIn(30, 150)).apply()
    }

    fun setActiveDailyHours(hours: Float) {
        prefs.edit().putFloat("active_batch_daily_hours", hours).apply()
    }

    fun getBatchChaptersAsItems(batch: BatchCurriculum): List<ChapterItem> {
        if (batch.chapters.isEmpty()) {
            val isNeet = batch.examType.contains("NEET", ignoreCase = true) ||
                    batch.id.contains("neet", ignoreCase = true) ||
                    batch.id.contains("yakeen", ignoreCase = true)
            return if (isNeet) JeeData.NEET_CHAPTERS else JeeData.CHAPTERS
        }
        return batch.chapters.mapIndexed { index, plan ->
            val subjectTag = if (plan.classGrade.isNotBlank() && plan.classGrade != "Both" && !plan.classGrade.startsWith("Class")) {
                "${plan.classGrade} ${plan.subject}"
            } else if (plan.classGrade.isNotBlank() && plan.classGrade != "Both") {
                "${plan.classGrade} ${plan.subject}"
            } else {
                plan.subject
            }
            ChapterItem(
                id = index + 1,
                subject = subjectTag,
                title = plan.chapterName,
                lectures = "${plan.totalLectures} Lec",
                durationHours = "${plan.estimatedHours.toInt()}h",
                questionFocus = "${plan.priority} • ${plan.subject} Core Mastery",
                priority = if (plan.priority.contains("High", ignoreCase = true)) "HIGH" else "MED"
            )
        }
    }

    fun getActiveBatchTitle(): String {
        return prefs.getString("active_batch_title", "PW Lakshya JEE 2026 (Class 12th)") ?: "PW Lakshya JEE 2026"
    }

    fun getActiveTargetDays(): Int {
        return prefs.getInt("active_batch_days", 60).coerceIn(30, 150)
    }

    fun getActiveDailyHours(): Float {
        return prefs.getFloat("active_batch_daily_hours", 12f)
    }

    fun isChapterComplete(batchId: String, chapterName: String): Boolean {
        val key = "batch_chap_${batchId}_${chapterName.trim().lowercase()}"
        return prefs.getBoolean(key, false)
    }

    fun setChapterComplete(batchId: String, chapterName: String, complete: Boolean) {
        val key = "batch_chap_${batchId}_${chapterName.trim().lowercase()}"
        prefs.edit().putBoolean(key, complete).apply()
    }

    fun toggleChapterComplete(batchId: String, chapterName: String): Boolean {
        val current = isChapterComplete(batchId, chapterName)
        setChapterComplete(batchId, chapterName, !current)
        return !current
    }

    fun getBatchCompletionStats(batch: BatchCurriculum): BatchCompletionStats {
        val chapters = batch.chapters
        val totalChapters = chapters.size
        var completedChapters = 0
        var completedLectures = 0

        chapters.forEach { chap ->
            if (isChapterComplete(batch.id, chap.chapterName)) {
                completedChapters++
                completedLectures += chap.totalLectures
            }
        }

        val overallPercentage = if (totalChapters > 0) {
            ((completedChapters.toFloat() / totalChapters) * 100f).roundToInt()
        } else 0

        val subjects = chapters.map { it.subject }.distinct()
        val subjectProgressList = subjects.map { subj ->
            val subjChaps = chapters.filter { it.subject == subj }
            val subjTotal = subjChaps.size
            val subjCompleted = subjChaps.count { isChapterComplete(batch.id, it.chapterName) }
            val subjTotalLec = subjChaps.sumOf { it.totalLectures }
            val subjCompletedLec = subjChaps.filter { isChapterComplete(batch.id, it.chapterName) }.sumOf { it.totalLectures }
            val subjPct = if (subjTotal > 0) ((subjCompleted.toFloat() / subjTotal) * 100f).roundToInt() else 0
            SubjectBatchProgress(
                subject = subj,
                completedChapters = subjCompleted,
                totalChapters = subjTotal,
                completedLectures = subjCompletedLec,
                totalLectures = subjTotalLec,
                percentage = subjPct
            )
        }

        return BatchCompletionStats(
            batchId = batch.id,
            totalChapters = totalChapters,
            completedChapters = completedChapters,
            totalLectures = batch.totalLectures,
            completedLectures = completedLectures,
            overallPercentage = overallPercentage,
            subjectProgress = subjectProgressList
        )
    }
}
