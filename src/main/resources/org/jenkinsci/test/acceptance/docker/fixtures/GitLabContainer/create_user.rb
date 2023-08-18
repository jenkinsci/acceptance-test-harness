user = User.create();
user.name = 'test';
user.username = 'test';
user.password = 'oparolaoarecare12!';
user.confirmed_at = '01/01/1990';
user.admin = true;
user.email = '$test@example.com';
user.save!;

token = user.personal_access_tokens.create(scopes: [:api], name: 'MyToken');
token.expires_at='01/01/2024';
token.save!;
puts token.token;
