# meat of the rspec glue code is defined in lib/jenkins/rspec.rb for better reuse in other gems
$:.unshift File.expand_path(File.dirname(__FILE__) + '/../lib')
require 'jenkins/rspec'
