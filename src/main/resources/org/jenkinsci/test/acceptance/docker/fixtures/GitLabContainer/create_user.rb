input_array = ARGV

user = User.create();
user.name = input_array[0];
user.username = input_array[0];
user.password = input_array[1];
user.confirmed_at = '01/01/1990';
user.admin = input_array[3];
user.email = input_array[2];
user.save!;

token = user.personal_access_tokens.create(scopes: [:api], name: 'MyToken');
token.expires_at='01/01/2024';
token.save!;
puts token.token;
