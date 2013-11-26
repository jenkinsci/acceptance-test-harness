require File.expand_path('../lib/jenkins/version', __FILE__)

Gem::Specification.new do |gem|
  gem.authors       = ['Jenkins project']
  gem.description   = %q{End-to-end test harness for Jenkins}
  gem.summary       = %q{Ruby + CLOUD = Blimpy}
  gem.homepage      = "https://github.com/jenkinsci/selenium-tests"

  gem.files         = `git ls-files`.split($\)
  gem.test_files    = gem.files.grep(%r{^(test|spec|features)/})
  gem.name          = "jenkins-selenium-tests"
  gem.require_paths = ["lib"]
  gem.version       = Jenkins::VERSION

  gem.add_dependency "rake"
  gem.add_dependency "selenium-webdriver"
  gem.add_dependency "sauce-cucumber"
  gem.add_dependency "rest-client"
  gem.add_dependency "pry"

  gem.add_dependency "cucumber", '1.2.5'
  gem.add_dependency "capybara", "~> 2.1.0"
  gem.add_dependency "json"
  gem.add_dependency "rspec"
  gem.add_dependency "tempdir"
  gem.add_dependency "mail"
  gem.add_dependency "promise"
  gem.add_dependency "vagrant", '~> 1.0'
  gem.add_dependency "jiraSOAP"    # for testing JIRA plugin
  gem.add_dependency "rdoc"

  gem.add_dependency "aspector"
end
