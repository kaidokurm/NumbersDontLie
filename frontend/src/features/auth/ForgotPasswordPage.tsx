import { useState } from 'react';
import { Link } from 'react-router-dom';
import { requestPasswordReset } from '../../api/auth';
import { Alert } from '../../shared/ui/Alert';
import { Card } from '../../shared/ui/Card';
import { Spinner } from '../../shared/ui/Spinner';
import { TextField } from '../../shared/ui/TextField';

export function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);

        if (!email.trim()) {
            setError('Email is required');
            return;
        }

        setLoading(true);
        try {
            const response = await requestPasswordReset(email.trim());
            setSuccess(response.message);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Could not request password reset');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-linear-to-b from-blue-50 to-white p-4">
            <Card>
                <div className="w-full max-w-sm p-8">
                    <h1 className="text-2xl font-bold text-slate-800 mb-2">Forgot Password</h1>
                    <p className="text-sm text-slate-600 mb-6">Enter your email and we will send reset instructions.</p>

                    {error && <Alert tone="error" title="Error" message={error} />}
                    {success && <Alert tone="success" title="Request Sent" message={success} />}

                    <form onSubmit={onSubmit} className="mt-4 space-y-4">
                        <TextField
                            label="Email"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="you@example.com"
                            disabled={loading}
                            required
                        />

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded font-semibold disabled:opacity-50"
                        >
                            {loading ? <Spinner label="Sending..." /> : 'Send Reset Link'}
                        </button>
                    </form>

                    <div className="mt-6 text-sm text-center">
                        <Link to="/" className="text-green-600 hover:text-green-700 font-semibold">
                            Back to Login
                        </Link>
                    </div>
                </div>
            </Card>
        </div>
    );
}
