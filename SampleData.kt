package com.example.data.repository

import com.example.data.model.Category
import com.example.data.model.Flashcard
import com.example.data.model.Question

object SampleData {
    val questions: List<Question> = listOf(
        // SCIENCE
        Question(
            id = "sci_1",
            category = Category.SCIENCE,
            question = "Which organelle is widely known as the 'powerhouse of the cell' due to its role in producing ATP?",
            options = listOf("Ribosome", "Mitochondria", "Endoplasmic Reticulum", "Golgi Apparatus"),
            correctIndex = 1,
            explanation = "Mitochondria generate most of the chemical energy (adenosine triphosphate or ATP) needed to power the cell's biochemical reactions.",
            difficulty = "Foundation",
            subtopic = "Biology"
        ),
        Question(
            id = "sci_2",
            category = Category.SCIENCE,
            question = "What is the escape velocity required for an object to break free from Earth's gravitational field at sea level?",
            options = listOf("7.9 km/s", "11.2 km/s", "16.7 km/s", "29.8 km/s"),
            correctIndex = 1,
            explanation = "The escape velocity from Earth's surface is approximately 11.2 km/s (about 25,000 mph).",
            difficulty = "Advanced",
            subtopic = "Physics"
        ),
        Question(
            id = "sci_3",
            category = Category.SCIENCE,
            question = "Which element has the highest electrical conductivity of all metals at room temperature?",
            options = listOf("Copper", "Gold", "Silver", "Aluminum"),
            correctIndex = 2,
            explanation = "Silver has the highest electrical and thermal conductivity of all known metals, followed closely by copper and gold.",
            difficulty = "Intermediate",
            subtopic = "Chemistry"
        ),
        Question(
            id = "sci_4",
            category = Category.SCIENCE,
            question = "What primary gas makes up approximately 78% of the Earth's atmosphere?",
            options = listOf("Oxygen", "Carbon Dioxide", "Argon", "Nitrogen"),
            correctIndex = 3,
            explanation = "Nitrogen constitutes about 78.08% of Earth's troposphere, while Oxygen constitutes roughly 20.95%.",
            difficulty = "Foundation",
            subtopic = "Earth Science"
        ),
        Question(
            id = "sci_5",
            category = Category.SCIENCE,
            question = "According to Newton's First Law of Motion, an object at rest will remain at rest unless acted upon by:",
            options = listOf("Balanced internal forces", "An external unbalanced force", "A constant velocity", "Gravitational equilibrium"),
            correctIndex = 1,
            explanation = "Newton's First Law (Law of Inertia) states that a body remains at rest or in uniform motion along a straight line unless compelled to change its state by an external unbalanced force.",
            difficulty = "Foundation",
            subtopic = "Physics"
        ),
        Question(
            id = "sci_6",
            category = Category.SCIENCE,
            question = "What is the chemical formula of ozone, the atmospheric molecule that shields Earth from harmful ultraviolet radiation?",
            options = listOf("O2", "O3", "H2O2", "CO3"),
            correctIndex = 1,
            explanation = "Ozone consists of three oxygen atoms (O3). In the stratosphere, it forms the ozone layer absorbing UV-B rays.",
            difficulty = "Foundation",
            subtopic = "Chemistry"
        ),

        // GENERAL KNOWLEDGE & HERITAGE
        Question(
            id = "gk_1",
            category = Category.GK,
            question = "Who was the architect who served as the Chairman of the Drafting Committee of the Indian Constitution?",
            options = listOf("Dr. B.R. Ambedkar", "Jawaharlal Nehru", "Dr. Rajendra Prasad", "Sardar Vallabhbhai Patel"),
            correctIndex = 0,
            explanation = "Dr. Bhimrao Ramji Ambedkar headed the Drafting Committee and is acclaimed as the Chief Architect of the Constitution of India.",
            difficulty = "Foundation",
            subtopic = "History & Polity"
        ),
        Question(
            id = "gk_2",
            category = Category.GK,
            question = "Which ancient astronomical observatory in Jaipur, India, featuring the world's largest stone sundial, is a UNESCO World Heritage site?",
            options = listOf("Qutub Minar", "Jantar Mantar", "Gol Gumbaz", "Konark Sun Temple"),
            correctIndex = 1,
            explanation = "Jantar Mantar in Jaipur was built by Rajput king Sawai Jai Singh II and completed in 1734. It features the Vrihat Samrat Yantra sundial.",
            difficulty = "Intermediate",
            subtopic = "Heritage"
        ),
        Question(
            id = "gk_3",
            category = Category.GK,
            question = "Who was the first Indian citizen to win the Nobel Prize in Physics in 1930 for the discovery of scattering of light?",
            options = listOf("Homi J. Bhabha", "Jagadish Chandra Bose", "Sir C.V. Raman", "Satyendra Nath Bose"),
            correctIndex = 2,
            explanation = "Sir Chandrasekhara Venkata Raman received the 1930 Nobel Prize in Physics for discovering the Raman Effect, celebrated annually as National Science Day on February 28.",
            difficulty = "Intermediate",
            subtopic = "Science Pioneers"
        ),
        Question(
            id = "gk_4",
            category = Category.GK,
            question = "What is the deepest known oceanic trench on planet Earth?",
            options = listOf("Puerto Rico Trench", "Java Trench", "Mariana Trench", "Tonga Trench"),
            correctIndex = 2,
            explanation = "The Challenger Deep in the Mariana Trench reaches a depth of approximately 10,994 meters (36,070 feet) in the western Pacific Ocean.",
            difficulty = "Foundation",
            subtopic = "Geography"
        ),

        // UN INFORMATION & GLOBAL GOALS
        Question(
            id = "un_1",
            category = Category.UN_INFO,
            question = "In which year was the United Nations officially established following the signing of the UN Charter?",
            options = listOf("1919", "1942", "1945", "1950"),
            correctIndex = 2,
            explanation = "The United Nations was founded on October 24, 1945, after the charter was ratified by China, France, Soviet Union, UK, US, and a majority of signatories. October 24 is commemorated as UN Day.",
            difficulty = "Foundation",
            subtopic = "UN History"
        ),
        Question(
            id = "un_2",
            category = Category.UN_INFO,
            question = "How many permanent members make up the United Nations Security Council (UNSC) with veto power?",
            options = listOf("3", "5", "7", "10"),
            correctIndex = 1,
            explanation = "The five permanent members (P5) of the UNSC are China, France, Russian Federation, the United Kingdom, and the United States.",
            difficulty = "Foundation",
            subtopic = "UN Organs"
        ),
        Question(
            id = "un_3",
            category = Category.UN_INFO,
            question = "Which specialized agency of the United Nations is tasked with promoting international education, sciences, and culture?",
            options = listOf("UNICEF", "UNESCO", "UNHCR", "UNEP"),
            correctIndex = 1,
            explanation = "UNESCO (United Nations Educational, Scientific and Cultural Organization) was founded in 1945 to build peace through international cooperation in education, sciences, and culture.",
            difficulty = "Intermediate",
            subtopic = "Specialized Agencies"
        ),
        Question(
            id = "un_4",
            category = Category.UN_INFO,
            question = "How many Sustainable Development Goals (SDGs) were adopted by all UN Member States in the 2030 Agenda?",
            options = listOf("10 Goals", "15 Goals", "17 Goals", "21 Goals"),
            correctIndex = 2,
            explanation = "The 2030 Agenda for Sustainable Development comprises 17 Goals and 169 targets addressing poverty, hunger, quality education, climate action, and peace.",
            difficulty = "Foundation",
            subtopic = "SDGs"
        ),
        Question(
            id = "un_5",
            category = Category.UN_INFO,
            question = "Where is the principal headquarters of the International Court of Justice (ICJ) located?",
            options = listOf("New York City, USA", "Geneva, Switzerland", "The Hague, Netherlands", "Vienna, Austria"),
            correctIndex = 2,
            explanation = "The International Court of Justice (World Court) sits at the Peace Palace in The Hague, Netherlands. It is the only principal organ not in New York.",
            difficulty = "Intermediate",
            subtopic = "UN Organs"
        ),

        // MENTAL MATH & LOGICAL REASONING
        Question(
            id = "math_1",
            category = Category.MATH_LOGIC,
            question = "Find the missing number in the sequence: 3, 7, 15, 31, 63, ?",
            options = listOf("95", "127", "129", "125"),
            correctIndex = 1,
            explanation = "Each term follows the pattern: Next = (Current * 2) + 1. Therefore, 63 * 2 + 1 = 126 + 1 = 127.",
            difficulty = "Intermediate",
            subtopic = "Number Series"
        ),
        Question(
            id = "math_2",
            category = Category.MATH_LOGIC,
            question = "If the word 'LIGHT' is coded as 'MJHIU' (+1 for each letter), how would 'SPARK' be coded?",
            options = listOf("TQBSL", "ROZQJ", "TQBRK", "TQCSL"),
            correctIndex = 0,
            explanation = "Shift each letter forward by 1: S->T, P->Q, A->B, R->S, K->L. Thus, 'TQBSL'.",
            difficulty = "Foundation",
            subtopic = "Coding Decoding"
        ),
        Question(
            id = "math_3",
            category = Category.MATH_LOGIC,
            question = "A car travels at 72 km/h. What is its speed converted into meters per second (m/s)?",
            options = listOf("15 m/s", "20 m/s", "25 m/s", "30 m/s"),
            correctIndex = 1,
            explanation = "To convert km/h to m/s, multiply by 5/18. 72 * (5/18) = 4 * 5 = 20 m/s.",
            difficulty = "Intermediate",
            subtopic = "Quantitative Speed"
        ),
        Question(
            id = "math_4",
            category = Category.MATH_LOGIC,
            question = "If all Blooms are Flowers, and some Flowers are Red, which of the following is logically guaranteed?",
            options = listOf("All Blooms are Red", "Some Blooms are Red", "Some Red things are Flowers", "No Blooms are Red"),
            correctIndex = 2,
            explanation = "Because 'some Flowers are Red' is given, it is logically convertible to 'some Red things are Flowers'. We cannot assume any intersection with Blooms without additional information.",
            difficulty = "Advanced",
            subtopic = "Syllogisms"
        ),

        // CYBER & DIGITAL TECH
        Question(
            id = "it_1",
            category = Category.CYBER_IT,
            question = "What is the decimal equivalent of the 8-bit binary number 10101000?",
            options = listOf("168", "160", "172", "154"),
            correctIndex = 0,
            explanation = "128 + 0 + 32 + 0 + 8 + 0 + 0 + 0 = 168.",
            difficulty = "Intermediate",
            subtopic = "Binary Systems"
        ),
        Question(
            id = "it_2",
            category = Category.CYBER_IT,
            question = "In cybersecurity, what does the 'S' in HTTPS stand for?",
            options = listOf("System", "Standard", "Secure", "Server"),
            correctIndex = 2,
            explanation = "HTTPS stands for HyperText Transfer Protocol Secure. It uses TLS/SSL encryption to protect web traffic.",
            difficulty = "Foundation",
            subtopic = "Cyber Security"
        ),
        Question(
            id = "it_3",
            category = Category.CYBER_IT,
            question = "Which data structure follows the First-In, First-Out (FIFO) principle?",
            options = listOf("Stack", "Queue", "Binary Tree", "Hash Map"),
            correctIndex = 1,
            explanation = "A Queue adheres to FIFO, where the first element added is the first to be removed. A Stack uses LIFO (Last-In, First-Out).",
            difficulty = "Foundation",
            subtopic = "Data Structures"
        )
    )

    val flashcards: List<Flashcard> = listOf(
        Flashcard(
            id = "fc_1",
            category = Category.SCIENCE,
            title = "Newton's Third Law",
            concept = "Action and Reaction",
            details = "Whenever one object exerts a force on another object, the second object exerts an equal and opposite force on the first.",
            keyTakeaway = "Forces always occur in matched pairs (Fab = -Fba)."
        ),
        Flashcard(
            id = "fc_2",
            category = Category.SCIENCE,
            title = "Photosynthesis Equation",
            concept = "Conversion of Light Energy",
            details = "6 CO2 + 6 H2O + Light Energy -> C6H12O6 (Glucose) + 6 O2. Takes place inside the chloroplasts.",
            keyTakeaway = "Produces oxygen and stored chemical energy."
        ),
        Flashcard(
            id = "fc_3",
            category = Category.UN_INFO,
            title = "UN Charter & Founding",
            concept = "San Francisco Conference 1945",
            details = "Signed on 26 June 1945 and entered into force on 24 October 1945. It outlines the purposes, principles, and organs of the United Nations.",
            keyTakeaway = "Primary mission: maintain international peace and security."
        ),
        Flashcard(
            id = "fc_4",
            category = Category.UN_INFO,
            title = "SDG Goal 4: Quality Education",
            concept = "Inclusive & Equitable Education",
            details = "Ensure inclusive and equitable quality education and promote lifelong learning opportunities for all children and youth by 2030.",
            keyTakeaway = "Universal foundational literacy and numeracy."
        ),
        Flashcard(
            id = "fc_5",
            category = Category.GK,
            title = "Indian National Emblem",
            concept = "Lion Capital of Ashoka",
            details = "Adopted on 26 January 1950 from the Sarnath Pillar erected by Emperor Ashoka. Features four Asiatic lions and the motto 'Satyameva Jayate'.",
            keyTakeaway = "Truth Alone Triumphs (Mundaka Upanishad)."
        ),
        Flashcard(
            id = "fc_6",
            category = Category.MATH_LOGIC,
            title = "Speed, Distance & Time",
            concept = "Unit Conversions",
            details = "Speed = Distance / Time. To convert km/h to m/s, multiply by 5/18. To convert m/s to km/h, multiply by 18/5.",
            keyTakeaway = "36 km/h = 10 m/s."
        ),
        Flashcard(
            id = "fc_7",
            category = Category.CYBER_IT,
            title = "Two-Factor Authentication",
            concept = "Defense-in-Depth Security",
            details = "2FA combines something you know (password) with something you have (authenticator app or token) or something you are (biometrics).",
            keyTakeaway = "Mitigates over 99% of automated credential attacks."
        )
    )
}
