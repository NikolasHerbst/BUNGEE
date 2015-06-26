BUNGEE is a Java based framework for benchmarking elasticity of IaaS cloud platforms. The tool automates the following benchmarking activities:

    A system analysis evaluates the load processing capabilities of the benchmarked platform at different scaling stages.
    The benchmark calibration uses the system analysis results and adjusts a given load intensity profile in a system specific manner.
    The measurement activity exposes the platform to a varying load according to the adjusted intensity profile.
    The elasticity evaluation measures the quality of the observed elastic behavior using a set of elasticity metrics.

At the moment, BUNGEE supports to analyse the elasticity of CloudStack and Amazon Web Service (AWS) based clouds that scale CPU-bound virtual machines horizontally.

For more information, please refer to http://descartes.tools/bungee 
