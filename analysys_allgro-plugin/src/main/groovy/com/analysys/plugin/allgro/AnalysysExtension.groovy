package com.analysys.plugin.allgro

class AnalysysExtension {

    public boolean lambdaEnabled = false
    public boolean checkXMLOnClick = false

    public Set<String> ignorePackage = []
    public Set<String> includePackage = []


    @Override
    String toString() {
        return "{" +
                "lambdaEnabled=${lambdaEnabled}\n" +
                "ignorePackage=${ignorePackage}\n" +
                "includePackage=${includePackage}\n" +
                "}"

    }
}

