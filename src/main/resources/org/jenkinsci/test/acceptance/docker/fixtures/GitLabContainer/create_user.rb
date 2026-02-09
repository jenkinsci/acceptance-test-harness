# frozen_string_literal: true

# Script to create GitLab user with personal access token
# Usage: gitlab-rails runner create_user.rb <username> <password> <email> <is_admin>

username, password, email, is_admin = ARGV

# Ensure default organization exists
default_org = Organizations::Organization.find_or_create_by!(name: 'Default', path: 'default') do |org|
  org.description = 'Default organization for test users'
end

# Create user with namespace and organization
user_params = {
  name: username,
  username: username,
  password: password,
  password_confirmation: password,
  email: email,
  admin: is_admin == 'true',
  skip_confirmation: true,
  organization_id: default_org&.id
}.compact

result = Users::CreateService.new(nil, user_params).execute

raise "Failed to create user: #{result.message}" unless result.success?

user = result.payload[:user]

raise "Failed to create user. Result: #{result.inspect}" unless user&.persisted?

# Create personal access token with 1-month expiration
token = user.personal_access_tokens.create!(
  scopes: [:api, :read_user, :read_api, :read_repository, :write_repository],
  name: 'MyToken',
  expires_at: 30.days.from_now
)

puts token.token
