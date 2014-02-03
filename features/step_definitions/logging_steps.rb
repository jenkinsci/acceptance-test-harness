When /^I create logger "(.*?)" logging$/ do |name, config|
  Jenkins::Logger.create(name, config.rows_hash)
end
