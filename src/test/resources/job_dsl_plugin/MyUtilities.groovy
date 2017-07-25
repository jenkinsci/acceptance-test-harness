package utilities

class MyUtilities {
    static void addDescription(def job) {
        job.with {
            description('Description from class in src folder.')
        }
    }
}
